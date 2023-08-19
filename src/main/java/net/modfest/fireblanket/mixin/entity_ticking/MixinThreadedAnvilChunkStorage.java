package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.modfest.fireblanket.world.entity.EntityFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {
	@Redirect(method = "loadEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;alwaysUpdateVelocity()Z"))
	private boolean fireblanket$forceVelocityOff(EntityType<?> instance) {
		if (EntityFilters.isTypeForcedVelocityOff(instance)) {
			return false;
		}

		return instance.alwaysUpdateVelocity();
	}
}
