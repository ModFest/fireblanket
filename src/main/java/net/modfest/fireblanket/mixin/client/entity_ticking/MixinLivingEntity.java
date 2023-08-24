package net.modfest.fireblanket.mixin.client.entity_ticking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {
    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = {"updateTrackedPositionAndAngles", "updateTrackedHeadRotation"}, at = @At(value = "HEAD"), ordinal = 0)
    private int fireblanket$smoothOutClientMovement(int interpolationSteps) {
        return Math.max(interpolationSteps, this.getType().getTrackTickInterval());
    }
}
