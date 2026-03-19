package net.modfest.fireblanket.mixin.mods.terracotta_knights;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "io.github.Bubblie01.terracotta_knights.entities.ai.ItemPickupGoal")
@Pseudo
public abstract class MixinItemPickupGoal extends Goal {
	@Redirect(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/sensing/Sensing;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean fireblanket$canAlwaysSee(Sensing inst, Entity e) {
		return true;
	}
}
