package net.modfest.fireblanket;

import java.util.concurrent.CompletableFuture;

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
import net.modfest.fireblanket.client.command.BERMaskCommand;
import net.modfest.fireblanket.client.command.ClientRegionCommand;
import net.modfest.fireblanket.client.command.EntityMaskCommand;
import net.modfest.fireblanket.client.screen.PlaceCommandBlockScreen;
import net.modfest.fireblanket.mixin.accessor.ClientLoginNetworkHandlerAccessor;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest;
import net.modfest.fireblanket.world.render_regions.RenderRegions;

public class FireblanketClient implements ClientModInitializer {
	
	public static final RenderRegions renderRegions = new RenderRegions();
	
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> {
			LiteralArgumentBuilder<FabricClientCommandSource> base = ClientCommandManager.literal("fireblanket:client");
			if (FireblanketMixin.DO_MASKING) {
				LiteralArgumentBuilder<FabricClientCommandSource> mask = ClientCommandManager.literal("mask");
				BERMaskCommand.init(mask, access);
				EntityMaskCommand.init(mask, access);
				ClientRegionCommand.init(base, access);
				base.then(mask);
			}
			dispatcher.register(ClientCommandManager.literal("fbc")
					.redirect(dispatcher.register(base)));
		});

		ClientLoginNetworking.registerGlobalReceiver(Fireblanket.FULL_STREAM_COMPRESSION, (client, handler, buf, listenerAdder) -> {
			if (Fireblanket.CAN_USE_ZSTD) {
				((FSCConnection)((ClientLoginNetworkHandlerAccessor)handler).fireblanket$getConnection()).fireblanket$enableFullStreamCompression();
				return CompletableFuture.completedFuture(PacketByteBufs.empty());
			} else {
				return CompletableFuture.completedFuture(null);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Fireblanket.BATCHED_BE_UPDATE, (client, handler, buf, sender) -> {
			int size = buf.readVarInt();

			for (int i = 0; i < size; i++) {
				BlockEntityUpdateS2CPacket fakePacket = new BlockEntityUpdateS2CPacket(buf);
				client.execute(() -> handler.onBlockEntityUpdate(fakePacket));
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Fireblanket.REGIONS_UPDATE, (client, handler, buf, sender) -> {
			RegionSyncRequest command = RegionSyncRequest.read(buf);
			if (command.valid()) {
				client.send(() -> {
					command.apply(renderRegions);
				});
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(Fireblanket.PLACE_COMMAND_BLOCK, (client, handler, buf, sender) -> {
			client.execute(() -> MinecraftClient.getInstance().setScreen(new PlaceCommandBlockScreen()));
		});
		
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			client.send(() -> {
				renderRegions.clear();
			});
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
