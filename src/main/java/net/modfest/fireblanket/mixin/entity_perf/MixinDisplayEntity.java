package net.modfest.fireblanket.mixin.entity_perf;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.class)
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
	public boolean shouldRenderAtSqrDistance(double distance) {
		return distance < Mth.square(fireblanket$viewDist * 64.0 * Entity.getViewScale());
	}
}
