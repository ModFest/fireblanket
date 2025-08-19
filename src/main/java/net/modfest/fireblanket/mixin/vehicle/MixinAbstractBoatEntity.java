package net.modfest.fireblanket.mixin.vehicle;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.modfest.fireblanket.mixinsupport.NonVehicleEnteringLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoatEntity.class)
public class MixinAbstractBoatEntity {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasVehicle()Z"))
	private boolean fireblanket$disallowEnteringBoats(boolean original, @Local Entity entity) {
		return original || entity instanceof LivingEntity && ((NonVehicleEnteringLivingEntity)entity).nonVehicleEntering();
	}
}
