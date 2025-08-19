package net.modfest.fireblanket.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.modfest.fireblanket.mixinsupport.NonVehicleEnteringLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperimentalMinecartController.class)
public class MixinExperimentalMinecartController {
	@ModifyExpressionValue(method = "pickUpEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"))
	private boolean fireblanket$disallowEnteringBoats(boolean original, @Local Entity entity) {
		return original || entity instanceof LivingEntity && ((NonVehicleEnteringLivingEntity)entity).nonVehicleEntering();
	}
}
