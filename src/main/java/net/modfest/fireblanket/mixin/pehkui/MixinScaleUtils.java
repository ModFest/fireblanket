package net.modfest.fireblanket.mixin.pehkui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.entity.Entity;

@Pseudo
@Mixin(targets="virtuoel.pehkui.util.ScaleUtils")
public class MixinScaleUtils {

	/**
	 * @reason Massive performance hazard.
	 * @author Una
	 */
	@Overwrite
	public static float getInteractionBoxWidthScale(Entity entity, float tickDelta) {
		return 1;
	}
	/**
	 * @reason Massive performance hazard.
	 * @author Una
	 */
	@Overwrite
	public static float getInteractionBoxHeightScale(Entity entity, float tickDelta) {
		return 1;
	}
	
}
