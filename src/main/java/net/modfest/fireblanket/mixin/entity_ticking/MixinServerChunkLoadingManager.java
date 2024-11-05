package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.modfest.fireblanket.config.EntityFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkLoadingManager.class)
public class MixinServerChunkLoadingManager {
	@Redirect(method = "loadEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;alwaysUpdateVelocity()Z"))
	private boolean fireblanket$forceVelocityOff(EntityType<?> instance) {
		if (EntityFilters.isTypeForcedVelocityOff(instance)) {
			return false;
		}

		return instance.alwaysUpdateVelocity();
	}
}
