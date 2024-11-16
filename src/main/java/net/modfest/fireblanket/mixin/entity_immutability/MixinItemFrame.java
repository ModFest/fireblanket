package net.modfest.fireblanket.mixin.entity_immutability;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.world.World;
import net.modfest.fireblanket.Fireblanket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFrameEntity.class)
public class MixinItemFrame {
	@Shadow
	private boolean fixed;

	@Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
	private void onInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		if (world.getGameRules().getBoolean(Fireblanket.NEW_ENTITIES_IMMUTABLE)) {
			this.fixed = true;
		}
	}
}
