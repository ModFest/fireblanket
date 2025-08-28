package net.modfest.fireblanket.mixin.entity_ticking;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements ImmmovableLivingEntity {
	@Shadow
	public abstract void kill(ServerWorld world);

	private boolean fireblanket$movementless = false;

	public MixinLivingEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "writeCustomData", at = @At("TAIL"))
	private void fireblanket$addToNbt(WriteView view, CallbackInfo ci) {
		if (fireblanket$movementless) {
			view.putBoolean("NoMovement", true);
		}
	}

	@Inject(method = "readCustomData", at = @At("TAIL"))
	private void fireblanket$readFromNbt(ReadView view, CallbackInfo ci) {
		fireblanket$movementless = view.getBoolean("NoMovement", false);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isRemoved()Z", ordinal = 0))
	private boolean fireblanket$dontTickMovement(LivingEntity instance) {
		return super.isRemoved() || fireblanket$movementless;
	}

	@Override
	public void setNoMovement(boolean noMovement) {
		this.fireblanket$movementless = noMovement;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;getDamagePerBlock()D"), method = "baseTick")
	private void doNotLeaveTheWorldBorderPlease(CallbackInfo ci, @Local ServerWorld serverWorld) {
		this.kill(serverWorld);
		this.setVelocity(Vec3d.ZERO);
		this.scheduleVelocityUpdate();
	}

	@Inject(at = @At("RETURN"), method = "tick")
	private void doNotGoTooFarPlease(CallbackInfo ci) {
		WorldBorder border = getWorld().getWorldBorder();
		double maxOutOfBoundsDistance = 64;

		int topY = getWorld().getHeight() - getWorld().getBottomY();
		double x = MathHelper.clamp(this.getX(), border.getBoundWest() - maxOutOfBoundsDistance, border.getBoundEast() + maxOutOfBoundsDistance);
		double y = MathHelper.clamp(this.getY(), getWorld().getBottomY() - maxOutOfBoundsDistance, topY + maxOutOfBoundsDistance * 2);
		double z = MathHelper.clamp(this.getZ(), border.getBoundNorth() - maxOutOfBoundsDistance, border.getBoundSouth() + maxOutOfBoundsDistance);
		if (x != this.getX() || y != this.getY() || z != this.getZ()) {
			this.setPosition(x, y, z);
		}
	}
}
