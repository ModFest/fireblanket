package net.modfest.fireblanket.mixin.entity_sync;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.modfest.fireblanket.mixinsupport.TrackerEntityHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
public abstract class MixinEntityTracker {
	@Shadow
	@Final
	EntityTrackerEntry entry;

	@Shadow
	@Final
	private Set<PlayerAssociatedNetworkHandler> listeners;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void fireblanket$setTrackerRef(ServerChunkLoadingManager serverChunkLoadingManager, Entity entity, int maxDistance, int tickInterval, boolean alwaysUpdateVelocity, CallbackInfo ci) {
		((TrackerEntityHolder)this.entry).setListeners(this.listeners);
	}
}
