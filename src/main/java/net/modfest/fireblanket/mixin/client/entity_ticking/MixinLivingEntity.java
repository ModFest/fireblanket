package net.modfest.fireblanket.mixin.client.entity_ticking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
	public MixinLivingEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@ModifyVariable(method = {"lerpHeadTo"}, at = @At(value = "HEAD"), ordinal = 0)
	private int fireblanket$smoothOutClientMovement(int interpolationSteps) {
		return Math.max(interpolationSteps, this.getType().updateInterval());
	}
}
