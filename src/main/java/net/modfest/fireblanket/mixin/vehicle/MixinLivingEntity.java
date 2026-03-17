package net.modfest.fireblanket.mixin.vehicle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.modfest.fireblanket.mixinsupport.NonVehicleEnteringLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements NonVehicleEnteringLivingEntity {
	private boolean fireblanket$nonVehicleEntering = false;

	public MixinLivingEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void fireblanket$addNoVehicleEnteringToNbt(ValueOutput view, CallbackInfo ci) {
		if (fireblanket$nonVehicleEntering) {
			view.putBoolean("NoVehicleEntering", true);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void fireblanket$readNoVehicleEnteringFromNbt(ValueInput view, CallbackInfo ci) {
		fireblanket$nonVehicleEntering = view.getBooleanOr("NoVehicleEntering", false);
	}

	public boolean nonVehicleEntering() {
		return fireblanket$nonVehicleEntering;
	}

	@Override
	public void setNoVehicleEntering(boolean cantEnterVehicles) {
		fireblanket$nonVehicleEntering = cantEnterVehicles;
	}
}
