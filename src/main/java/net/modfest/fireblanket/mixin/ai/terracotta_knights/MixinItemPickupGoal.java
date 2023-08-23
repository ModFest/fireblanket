package net.modfest.fireblanket.mixin.ai.terracotta_knights;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobVisibilityCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "io.github.Bubblie01.terracotta_knights.entities.ai.ItemPickupGoal")
@Pseudo
public abstract class MixinItemPickupGoal extends Goal {
	@Redirect(method = "method_6264", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobVisibilityCache;canSee(Lnet/minecraft/entity/Entity;)Z"))
	private boolean fireblanket$canAlwaysSee(MobVisibilityCache inst, Entity e) {
		return true;
	}
}
