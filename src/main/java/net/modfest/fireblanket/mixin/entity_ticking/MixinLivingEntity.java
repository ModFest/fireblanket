package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ImmmovableLivingEntity {
	private boolean fireblanket$movementless = false;

	public MixinLivingEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void fireblanket$addToNbt(ValueOutput view, CallbackInfo ci) {
		if (fireblanket$movementless) {
			view.putBoolean("NoMovement", true);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void fireblanket$readFromNbt(ValueInput view, CallbackInfo ci) {
		fireblanket$movementless = view.getBooleanOr("NoMovement", false);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isRemoved()Z", ordinal = 0))
	private boolean fireblanket$dontTickMovement(LivingEntity instance) {
		return super.isRemoved() || fireblanket$movementless;
	}

	@Override
	public void setNoMovement(boolean noMovement) {
		this.fireblanket$movementless = noMovement;
	}
}
