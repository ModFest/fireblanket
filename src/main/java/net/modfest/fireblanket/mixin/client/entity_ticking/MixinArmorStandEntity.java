package net.modfest.fireblanket.mixin.client.entity_ticking;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ArmorStand.class)
public abstract class MixinArmorStandEntity extends LivingEntity {
	protected MixinArmorStandEntity(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	// TODO: still useful?
//	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;tick()V"))
//	private void fireblanket$noClientTick(LivingEntity instance) {
//		if (this.getLevel().isClient) {
//			EntityTick.minimalLivingTick(instance);
//		} else {
//			super.tick();
//		}
//	}
}
