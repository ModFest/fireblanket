package net.modfest.fireblanket.mixin.entity_sync;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkManager;
import net.modfest.fireblanket.net.BatchedEntityVelocityUpdatePacket;
import net.modfest.fireblanket.net.ProtoVelocityUpdate;
import net.modfest.fireblanket.net.TrackerGlobal;
import net.modfest.fireblanket.net.VelocityUpdate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;tickEntityMovement()V", shift = At.Shift.BEFORE))
	public void fireblanket$tickEntityBefore(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
		TrackerGlobal.UPDATES.clear();
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkLoadingManager;tickEntityMovement()V", shift = At.Shift.AFTER))
	public void fireblanket$tickEntityAfter(BooleanSupplier shouldKeepTicking, boolean tickChunks, CallbackInfo ci) {
		if (!TrackerGlobal.UPDATES.isEmpty()) {

			Reference2ReferenceOpenHashMap<PlayerAssociatedNetworkHandler, List<VelocityUpdate>> map = new Reference2ReferenceOpenHashMap<>();
			for (ProtoVelocityUpdate update : TrackerGlobal.UPDATES) {
				for (PlayerAssociatedNetworkHandler listener : update.listeners()) {
					map.computeIfAbsent(listener, k -> new ArrayList<>()).add(update.update());
				}
			}

			for (Map.Entry<PlayerAssociatedNetworkHandler, List<VelocityUpdate>> e : map.entrySet()) {
				ServerPlayNetworking.send(e.getKey().getPlayer(), new BatchedEntityVelocityUpdatePacket(e.getValue()));
			}

			TrackerGlobal.UPDATES.clear();
		}
	}
}
