package net.modfest.fireblanket.mixin.entity_sync;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.modfest.fireblanket.mixinsupport.TrackerEntityHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class MixinEntityTracker {
	@Shadow
	@Final
	private ServerEntity serverEntity;

	@Shadow
	@Final
	private Set<ServerPlayerConnection> seenBy;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void fireblanket$setTrackerRef(final CallbackInfo ci) {
		((TrackerEntityHolder) this.serverEntity).fireblanket$setListeners(this.seenBy);
	}
}
