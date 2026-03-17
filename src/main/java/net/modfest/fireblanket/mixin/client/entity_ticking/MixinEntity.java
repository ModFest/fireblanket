package net.modfest.fireblanket.mixin.client.entity_ticking;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {
	@Inject(
		method = "moveOrInterpolateTo",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/InterpolationHandler;interpolateTo(Lnet/minecraft/world/phys/Vec3;FF)V")
	)
	public void fireblanket$smoothOutClientMovement(Vec3 pos, float yaw, float pitch, CallbackInfo ci, @Local(ordinal = 0) InterpolationHandler positionInterpolator) {
		positionInterpolator.setInterpolationLength(Math.max(positionInterpolator.interpolationSteps, ((Entity) (Object) this).getType().updateInterval()));
	}
}
