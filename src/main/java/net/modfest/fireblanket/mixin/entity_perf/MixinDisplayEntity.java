package net.modfest.fireblanket.mixin.entity_perf;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayEntity.class)
public abstract class MixinDisplayEntity {
	@Shadow
	public abstract float getViewRange();

	private float fireblanket$viewDist = 1;

	@Inject(method = "tick", at = @At("TAIL"))
	private void fireblanket$tick(CallbackInfo ci) {
		fireblanket$viewDist = this.getViewRange();
	}

	/**
	 * @author jaskarth
	 *
	 * @reason Avoid using the data tracker for speed
	 */
	@Overwrite
	public boolean shouldRender(double distance) {
		return distance < MathHelper.square(fireblanket$viewDist * 64.0 * Entity.getRenderDistanceMultiplier());
	}
}
