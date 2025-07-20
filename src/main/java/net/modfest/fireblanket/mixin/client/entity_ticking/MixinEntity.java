package net.modfest.fireblanket.mixin.client.entity_ticking;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
	@Inject(
		method="updateTrackedPositionAndAngles",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/PositionInterpolator;refreshPositionAndAngles(Lnet/minecraft/util/math/Vec3d;FF)V")
	)
	public void fireblanket$smoothOutClientMovement(Vec3d pos, float yaw, float pitch, CallbackInfo ci, @Local(ordinal=0) PositionInterpolator positionInterpolator) {
		positionInterpolator.setLerpDuration(Math.max(positionInterpolator.lerpDuration, ((Entity)(Object)this).getType().getTrackTickInterval()));
	}
}
