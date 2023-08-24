package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ImmmovableLivingEntity {
	private boolean fireblanket$movementless = false;

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void fireblanket$addToNbt(NbtCompound nbt, CallbackInfo ci) {
		if (fireblanket$movementless) {
			nbt.putBoolean("NoMovement", true);
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void fireblanket$readFromNbt(NbtCompound nbt, CallbackInfo ci) {
		fireblanket$movementless = nbt.getBoolean("NoMovement");
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isRemoved()Z", ordinal = 0))
	private boolean fireblanket$dontTickMovement(LivingEntity instance) {
		return super.isRemoved() || fireblanket$movementless;
	}

	@Override
	public void setNoMovement(boolean noMovement) {
		this.fireblanket$movementless = noMovement;
	}
}
