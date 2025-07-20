package net.modfest.fireblanket;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
//import net.modfest.fireblanket.client.command.BERMaskCommand;
import net.modfest.fireblanket.client.command.CountParticleTypesCommand;
//import net.modfest.fireblanket.client.command.EntityMaskCommand;
import net.modfest.fireblanket.client.command.TickTimeCommand;
import net.modfest.fireblanket.client.command.WireframeCommand;
import net.modfest.fireblanket.client.screen.PlaceCommandBlockScreen;
import net.modfest.fireblanket.mixin.accessor.ClientLoginNetworkHandlerAccessor;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.net.BEUpdate;
import net.modfest.fireblanket.net.BatchedBEUpdatePayload;
import net.modfest.fireblanket.net.BatchedEntityVelocityUpdatePacket;
import net.modfest.fireblanket.net.CommandBlockPacket;
import net.modfest.fireblanket.net.VelocityUpdate;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest;
import net.modfest.fireblanket.world.render_regions.RenderRegions;

import java.util.concurrent.CompletableFuture;

public class FireblanketClient implements ClientModInitializer {
	public static final boolean VERIFY_RENDER = true; // Disable for prod !!

	public static final RenderRegions renderRegions = new RenderRegions();

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("fireblanket:client");
//			if (FireblanketMixin.DO_MASKING) {
//				LiteralArgumentBuilder<FabricClientCommandSource> mask = ClientCommandManager.literal("mask");
//				BERMaskCommand.init(mask, access);
//				EntityMaskCommand.init(mask, access);
//				base.then(mask);
//			}
//			ClientRegionCommand.init(base, access);
			WireframeCommand.init(base, access);
			TickTimeCommand.init(base, access);
			CountParticleTypesCommand.init(base, access);

			dispatcher.register(ClientCommandManager.literal("fbc")
				.redirect(dispatcher.register(base)));
		});

		ClientLoginNetworking.registerGlobalReceiver(Fireblanket.FULL_STREAM_COMPRESSION, (client, handler, buf, listenerAdder) -> {
			if (Fireblanket.CAN_USE_ZSTD) {
				((FSCConnection) ((ClientLoginNetworkHandlerAccessor) handler).fireblanket$getConnection()).fireblanket$enableFullStreamCompression();
				return CompletableFuture.completedFuture(PacketByteBufs.empty());
			} else {
				return CompletableFuture.completedFuture(null);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(BatchedBEUpdatePayload.ID, (payload, context) -> {
			for (BEUpdate update : payload.updates()) {
				BlockEntityUpdateS2CPacket fakePacket = new BlockEntityUpdateS2CPacket(update.pos(), update.type(), update.nbt());
				context.client().execute(() -> context.client().getNetworkHandler().onBlockEntityUpdate(fakePacket));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(BatchedEntityVelocityUpdatePacket.ID, (payload, context) -> {
			for (VelocityUpdate update : payload.updates()) {
				int id = update.entity();
				double vx = update.getVelocityX();
				double vy = update.getVelocityY();
				double vz = update.getVelocityZ();

				context.client().execute(() -> {
					Entity entity = context.client().world.getEntityById(id);
					if (entity != null) {
						entity.setVelocityClient(vx, vy, vz);
					}
				});
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(RegionSyncRequest.ID, (payload, ctx) -> {
			if (payload.valid()) {
				ctx.client().send(() -> {
					payload.apply(renderRegions);
				});
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(CommandBlockPacket.ID, (payload, ctx) -> {
			ctx.client().execute(() -> MinecraftClient.getInstance().setScreen(new PlaceCommandBlockScreen()));
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			client.send(renderRegions::clear);
		});
	}

	public static boolean shouldRender(Entity entity) {
		Vec3d c = getCameraPos();
		return renderRegions.shouldRender(c.x, c.y, c.z, entity);
	}

	public static boolean shouldRender(BlockEntity entity) {
		Vec3d c = getCameraPos();
		return renderRegions.shouldRender(c.x, c.y, c.z, entity);
	}

	private static Vec3d getCameraPos() {
		MinecraftClient mc = MinecraftClient.getInstance();
		Vec3d c = mc.gameRenderer.getCamera().getPos();
		return c;
	}

}
