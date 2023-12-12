package net.modfest.fireblanket;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.modfest.fireblanket.command.CmdFindReplaceCommand;
import net.modfest.fireblanket.command.DumpCommand;
import net.modfest.fireblanket.command.RegionCommand;
import net.modfest.fireblanket.compat.PolyMcCompat;
import net.modfest.fireblanket.mixin.accessor.ClientConnectionAccessor;
import net.modfest.fireblanket.mixin.accessor.ServerChunkManagerAccessor;
import net.modfest.fireblanket.world.blocks.UpdateSignBlockEntityTypes;
import net.modfest.fireblanket.mixin.accessor.ServerLoginNetworkHandlerAccessor;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest;
import net.modfest.fireblanket.world.render_regions.RenderRegions;
import net.modfest.fireblanket.world.render_regions.RenderRegionsState;
import net.modfest.fireblanket.world.entity.EntityFilters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.google.common.base.Stopwatch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Fireblanket implements ModInitializer {
	public static final Identifier BATCHED_BE_UPDATE = new Identifier("fireblanket", "batched_be_sync");
	public static final Identifier FULL_STREAM_COMPRESSION = new Identifier("fireblanket", "full_stream_compression");
	public static final Identifier REGIONS_UPDATE = new Identifier("fireblanket", "regions_update");
	public static final Identifier PLACE_COMMAND_BLOCK = new Identifier("fireblanket", "place_command_block");

	public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");
	
	public record QueuedPacket(ClientConnection conn, Packet<?> packet, PacketCallbacks listener) {}
	
	private static final AtomicInteger nextQueue = new AtomicInteger();
	
	@SuppressWarnings("unchecked")
	public static final LinkedBlockingQueue<QueuedPacket>[] PACKET_QUEUES = new LinkedBlockingQueue[4];
	
	public static boolean CAN_USE_ZSTD = false;
	
	public static final ChunkTicketType<ChunkPos> KEEP_LOADED = ChunkTicketType.create("fireblanket:keep_loaded", ChunkTicketType.FORCED.getArgumentComparator());
	
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal("fireblanket");
			DumpCommand.init(base, access);
			RegionCommand.init(base, access);
			CmdFindReplaceCommand.init(base, access);
			dispatcher.register(CommandManager.literal("fb")
					.redirect(dispatcher.register(base)));
		});

		for (Block block : Registries.BLOCK) {
			UpdateSignBlockEntityTypes.apply(block);
		}

		RegistryEntryAddedCallback.event(Registries.BLOCK).register((r, id, block) -> {
			UpdateSignBlockEntityTypes.apply(block);
		});

		Path configs = FabricLoader.getInstance().getConfigDir().resolve("fireblanket");
		if (Files.exists(configs)) {
			Path types = configs.resolve("entityfilters.txt");

			if (Files.exists(types)) {
				EntityFilters.parse(types);
			}
		}
		
		for (int i = 0; i < PACKET_QUEUES.length; i++) {
			LinkedBlockingQueue<QueuedPacket> q = new LinkedBlockingQueue<>();
			PACKET_QUEUES[i] = q;
			Thread thread = new Thread(() -> {
				while (true) {
					try {
						QueuedPacket p = q.take();
						((ClientConnectionAccessor)p.conn()).fireblanket$sendImmediately(p.packet(), p.listener(), true);
					} catch (Throwable t) {
						LOGGER.error("Exception in packet thread", t);
					}
				}
			}, "Fireblanket async packet send thread #"+(i+1));
			thread.setDaemon(true);
			thread.start();
		}
		
		try {
			// TODO: ZSTD compression does not work with 1.20.2+ packet handling
//			Native.load();
//			CAN_USE_ZSTD = true;
		} catch (UnsatisfiedLinkError e) {
			CAN_USE_ZSTD = false;
			LOGGER.warn("Could not load zstd, full-stream compression unavailable", e);
		}
		
		if (CAN_USE_ZSTD) {
			LOGGER.info("Enabling full-stream compression");
			ServerLoginConnectionEvents.QUERY_START.addPhaseOrdering(new Identifier("fireblanket:pre"), Event.DEFAULT_PHASE);
			ServerLoginConnectionEvents.QUERY_START.register(new Identifier("fireblanket:pre"), (handler, server, sender, synchronizer) -> {
				if (!server.isSingleplayer()) {
					sender.sendPacket(FULL_STREAM_COMPRESSION, PacketByteBufs.empty());
				}
			});
		}
		
		ServerLoginNetworking.registerGlobalReceiver(FULL_STREAM_COMPRESSION, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (understood) {
				((FSCConnection)((ServerLoginNetworkHandlerAccessor)handler).fireblanket$getConnection()).fireblanket$enableFullStreamCompression();
			}
		});
		
		if (FabricLoader.getInstance().isModLoaded("polymc")) {
			PolyMcCompat.init();
		}
		
		ServerWorldEvents.LOAD.register((server, world) -> {
			if (System.getProperty("fireblanket.loadRadius") != null) {
				if (!world.getRegistryKey().getValue().toString().equals("minecraft:overworld")) return;
				int radius = Integer.getInteger("fireblanket.loadRadius");
				int min = (int)Math.floor(-radius/16);
				int max = (int)Math.ceil(radius/16);
				int count = (max-min)*(max-min);
				ChunkTicketManager mgr = ((ServerChunkManagerAccessor)world.getChunkManager()).fireblanket$getTicketManager();
				LOGGER.info("Forcing "+count+" chunks to stay loaded (but not ticking)...");
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
						if (System.nanoTime()-lastReport > 1_000_000_000) {
							lastReport = System.nanoTime();
							LOGGER.info(done+"/"+count+" loaded ("+((done*100)/count)+"%)...");
						}
					}
				}
				LOGGER.info("Done after "+sw);
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
		sender.accept(req.toPacket(REGIONS_UPDATE));
	}

	public static LinkedBlockingQueue<QueuedPacket> getNextQueue() {
		return PACKET_QUEUES[Math.floorMod(nextQueue.getAndIncrement(), PACKET_QUEUES.length)];
	}
}
