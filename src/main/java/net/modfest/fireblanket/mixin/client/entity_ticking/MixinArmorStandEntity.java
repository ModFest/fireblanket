package net.modfest.fireblanket.mixin.client.entity_ticking;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.world.World;
import net.modfest.fireblanket.world.entity.EntityTick;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ArmorStandEntity.class)
public abstract class MixinArmorStandEntity extends LivingEntity {
	protected MixinArmorStandEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tick()V"))
	private void fireblanket$noClientTick(LivingEntity instance) {
		if (this.getWorld().isClient) {
			EntityTick.minimalLivingTick(instance);
		} else {
			super.tick();
		}
	}
}
