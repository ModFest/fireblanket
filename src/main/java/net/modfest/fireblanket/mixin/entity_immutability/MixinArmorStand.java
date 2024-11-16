package net.modfest.fireblanket.mixin.entity_immutability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.world.World;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntity.class)
public class MixinArmorStand {
	@Shadow
	private int disabledSlots;

	@Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
	private void onInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		if (world.getGameRules().getBoolean(Fireblanket.NEW_ENTITIES_IMMUTABLE)) {
			// Disable all slots by default
			this.disabledSlots = 4144959;
			// Disable movement (prevents abuse of fishing rods)
			if (this instanceof ImmmovableLivingEntity im) {
				im.setNoMovement(true);
			}
		}
	}
}
