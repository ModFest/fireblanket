package net.modfest.fireblanket;

import com.github.luben.zstd.util.Native;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameRules;
import net.modfest.fireblanket.command.CmdFindReplaceCommand;
import net.modfest.fireblanket.command.DumpCommand;
import net.modfest.fireblanket.command.ItemBanCommand;
import net.modfest.fireblanket.command.RegionCommand;
import net.modfest.fireblanket.command.StareCommand;
import net.modfest.fireblanket.compat.PolyMcCompat;
import net.modfest.fireblanket.compat.roles.PlayerRolesCompat;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.mixin.accessor.ClientConnectionAccessor;
import net.modfest.fireblanket.mixin.accessor.ServerChunkManagerAccessor;
import net.modfest.fireblanket.mixin.accessor.ServerLoginNetworkHandlerAccessor;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.net.BatchedBEUpdatePayload;
import net.modfest.fireblanket.net.BatchedEntityVelocityUpdatePacket;
import net.modfest.fireblanket.net.CommandBlockPacket;
import net.modfest.fireblanket.util.LinkedBlocQueue;
import net.modfest.fireblanket.world.ItemBan;
import net.modfest.fireblanket.world.blocks.UpdateSignBlockEntityTypes;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest;
import net.modfest.fireblanket.world.render_regions.RenderRegions;
import net.modfest.fireblanket.world.render_regions.RenderRegionsState;
import net.modfest.fireblanket.config.EntityFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

public class Fireblanket implements ModInitializer {
	public static final Identifier BATCHED_BE_UPDATE = Identifier.of("fireblanket", "batched_be_sync");
	public static final Identifier BATCHED_VELOCITY_SYNC = Identifier.of("fireblanket", "batched_velocity_sync");
	public static final Identifier FULL_STREAM_COMPRESSION = Identifier.of("fireblanket", "full_stream_compression");
	public static final Identifier REGIONS_UPDATE = Identifier.of("fireblanket", "regions_update");

	/**
	 * Whether new entities will be fixed.
	 * @see net.modfest.fireblanket.mixin.entity_immutability
	 */
	public static final GameRules.Key<GameRules.BooleanRule> NEW_ENTITIES_IMMUTABLE =
		GameRuleRegistry.register("newEntitiesImmutable", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(true));

	public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");

	public record QueuedPacket(ClientConnection conn, Packet<?> packet, PacketCallbacks listener) {
	}

	private static final AtomicInteger nextQueue = new AtomicInteger();

	@SuppressWarnings("unchecked")
	public static LinkedBlocQueue<QueuedPacket>[] PACKET_QUEUES;

	public static boolean CAN_USE_ZSTD = false;
	public static boolean IS_FIREBLANKET_SERVER = false;

	public static final ChunkTicketType<ChunkPos> KEEP_LOADED = ChunkTicketType.create("fireblanket:keep_loaded", ChunkTicketType.FORCED.getArgumentComparator());

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal("fireblanket");
			DumpCommand.init(base, access);
			RegionCommand.init(base, access);
			CmdFindReplaceCommand.init(base, access);
			StareCommand.init(base, access);
			ItemBanCommand.init(base, access);

			dispatcher.register(CommandManager.literal("fb")
				.redirect(dispatcher.register(base)));
		});

		for (Block block : Registries.BLOCK) {
			UpdateSignBlockEntityTypes.apply(block);
		}

		RegistryEntryAddedCallback.event(Registries.BLOCK).register((r, id, block) -> {
			UpdateSignBlockEntityTypes.apply(block);
		});

		EntityFilters.init();
		ItemBan.apply();

		IS_FIREBLANKET_SERVER = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

		if (IS_FIREBLANKET_SERVER) {
			PACKET_QUEUES = new LinkedBlocQueue[FireblanketConfig.get(ConfigSpecs.ASYNC_PACKET_THREADS)];
			for (int i = 0; i < PACKET_QUEUES.length; i++) {
				LinkedBlocQueue<QueuedPacket> q = new LinkedBlocQueue<>();
				PACKET_QUEUES[i] = q;

				Thread thread = new Thread(() -> {
					while (true) {
						LinkedBlocQueue.Bloc<QueuedPacket> bloc = q.pull();
						if (bloc == null) {
							Thread.yield();
							LockSupport.parkNanos("Waiting for packets", 100_000L);
							continue;
						}

						if (bloc.size() > 100) {
							System.out.println(">> " + bloc.size() + " " + Thread.currentThread().getName());
						}

						LinkedBlocQueue.Node<QueuedPacket> node = bloc.node();
						do {
							try {
								QueuedPacket p = node.data;
								((ClientConnectionAccessor) p.conn()).fireblanket$sendImmediately(p.packet(), p.listener(), true);
								node = node.next;
							} catch (Throwable t) {
								LOGGER.error("Exception in packet thread", t);
							}
						} while(node != null);
					}
				}, "Fireblanket async packet send thread #" + (i + 1));

				thread.setDaemon(true);
				thread.start();
			}
		}

		try {
			if (!FireblanketConfig.get(ConfigSpecs.AVOID_ZSTD)) {
				Native.load();
				CAN_USE_ZSTD = true;
			}
		} catch (UnsatisfiedLinkError e) {
			CAN_USE_ZSTD = false;
			LOGGER.warn("Could not load zstd, full-stream compression unavailable", e);
		}

		// Networking
		PayloadTypeRegistry.playS2C().register(BatchedBEUpdatePayload.ID, BatchedBEUpdatePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(BatchedEntityVelocityUpdatePacket.ID, BatchedEntityVelocityUpdatePacket.CODEC);
		PayloadTypeRegistry.playS2C().register(CommandBlockPacket.ID, CommandBlockPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(RegionSyncRequest.ID, RegionSyncRequest.CODEC);

		if (CAN_USE_ZSTD) {
			LOGGER.info("Enabling full-stream compression");
			ServerLoginConnectionEvents.QUERY_START.addPhaseOrdering(Identifier.of("fireblanket:pre"), Event.DEFAULT_PHASE);
			ServerLoginConnectionEvents.QUERY_START.register(Identifier.of("fireblanket:pre"), (handler, server, sender, synchronizer) -> {
				if (!server.isSingleplayer()) {
					sender.sendPacket(FULL_STREAM_COMPRESSION, PacketByteBufs.empty());
				}
			});
		}

		ServerLoginNetworking.registerGlobalReceiver(FULL_STREAM_COMPRESSION, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (understood) {
				((FSCConnection) ((ServerLoginNetworkHandlerAccessor) handler).fireblanket$getConnection()).fireblanket$enableFullStreamCompression();
			}
		});

		if (FabricLoader.getInstance().isModLoaded("polymc")) {
			PolyMcCompat.init();
		}

		PlayerRolesCompat.init();

		ServerWorldEvents.LOAD.register((server, world) -> {
			if (FireblanketConfig.get(ConfigSpecs.FORCED_LOAD_RADIUS) > 0) {
				if (!world.getRegistryKey().getValue().toString().equals("minecraft:overworld")) return;
				int radius = FireblanketConfig.get(ConfigSpecs.FORCED_LOAD_RADIUS);
				int min = (int) Math.floor(-radius / 16);
				int max = (int) Math.ceil(radius / 16);
				int count = (max - min) * (max - min);
				ChunkTicketManager mgr = ((ServerChunkManagerAccessor) world.getChunkManager()).fireblanket$getTicketManager();
				LOGGER.info("Forcing " + count + " chunks to stay loaded (but not ticking)...");
				int done = 0;
				long lastReport = System.nanoTime();
				Stopwatch sw = Stopwatch.createStarted();
				for (int x = min; x <= max; x++) {
					for (int z = min; z <= max; z++) {
						ChunkPos pos = new ChunkPos(x, z);
						// poke the chunk so it loads; a ticket with a distance this high isn't enough to *cause* a load on its own
						world.getChunk(x, z);
						// one above FULL; out of range, but not so far to unload
						mgr.addTicketWithLevel(KEEP_LOADED, pos, 34, pos);
						done++;
						if (System.nanoTime() - lastReport > 1_000_000_000) {
							lastReport = System.nanoTime();
							LOGGER.info(done + "/" + count + " loaded (" + ((done * 100) / count) + "%)...");
						}
					}
				}
				LOGGER.info("Done after " + sw);
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			fullRegionSync(handler.player.getServerWorld(), sender::sendPacket);
		});

		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
			fullRegionSync(player.getServerWorld(), player.networkHandler::sendPacket);
		});
	}

	public static void fullRegionSync(ServerWorld world, Consumer<Packet<?>> sender) {
		RenderRegions regions = RenderRegionsState.get(world).getRegions();
		RegionSyncRequest req;
		if (regions.getRegionsByName().isEmpty()) {
			req = new RegionSyncRequest.Reset(true);
		} else {
			req = regions.toPacket();
		}
		sender.accept(ServerPlayNetworking.createS2CPacket(req));
	}

	public static LinkedBlocQueue<QueuedPacket> getNextQueue() {
		if (!IS_FIREBLANKET_SERVER) {
			return null;
		}

		return PACKET_QUEUES[Math.floorMod(nextQueue.getAndIncrement(), PACKET_QUEUES.length)];
	}
}
