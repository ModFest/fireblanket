package net.modfest.fireblanket.mixin.client.entity_ticking.portal_cubed;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import net.modfest.fireblanket.client.EntityTick;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.fusionflux.portalcubed.entity.CorePhysicsEntity")
@Pseudo
public abstract class MixinCorePhysicsEntity extends PathAwareEntity {
    protected MixinCorePhysicsEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // Mixin can't seem to remap this method properly- so we do its job for it
    @Redirect(method = "method_5773", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PathAwareEntity;tick()V"))
    private void fireblanket$noClientTick(PathAwareEntity instance) {
        if (this.getWorld().isClient) {
            EntityTick.minimalTick(instance);
        } else {
            instance.tick();
        }
    }
}
