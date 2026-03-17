package net.modfest.fireblanket.mixin.ai;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(TemptGoal.class)
public class MixinTemptGoal {
	/**
	 * @author Jasmine
	 * @reason Hey, at least it's better than injecting unconditionally at head.
	 */
	@Overwrite
	public boolean canUse() {
		return false;
	}
}
