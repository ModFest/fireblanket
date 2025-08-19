package net.modfest.fireblanket.mixin.vehicle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import net.modfest.fireblanket.mixinsupport.NonVehicleEnteringLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements NonVehicleEnteringLivingEntity {
	private boolean fireblanket$nonVehicleEntering = false;

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "writeCustomData", at = @At("TAIL"))
	private void fireblanket$addNoVehicleEnteringToNbt(WriteView view, CallbackInfo ci) {
		if (fireblanket$nonVehicleEntering) {
			view.putBoolean("NoVehicleEntering", true);
		}
	}

	@Inject(method = "readCustomData", at = @At("TAIL"))
	private void fireblanket$readNoVehicleEnteringFromNbt(ReadView view, CallbackInfo ci) {
		fireblanket$nonVehicleEntering = view.getBoolean("NoVehicleEntering", false);
	}

	public boolean nonVehicleEntering() {
		return fireblanket$nonVehicleEntering;
	}

	@Override
	public void setNoVehicleEntering(boolean cantEnterVehicles) {
		fireblanket$nonVehicleEntering = cantEnterVehicles;
	}
}
