package net.modfest.fireblanket.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.modfest.fireblanket.mixinsupport.NonVehicleEnteringLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NewMinecartBehavior.class)
public class MixinExperimentalMinecartController {
	@ModifyExpressionValue(method = "pickupEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z"))
	private boolean fireblanket$disallowEnteringBoats(boolean original, @Local Entity entity) {
		return original || entity instanceof LivingEntity && ((NonVehicleEnteringLivingEntity)entity).nonVehicleEntering();
	}
}
