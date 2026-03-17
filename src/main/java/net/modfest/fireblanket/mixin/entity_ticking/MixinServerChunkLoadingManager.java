package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.EntityType;
import net.modfest.fireblanket.config.EntityFilters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkMap.class)
public class MixinServerChunkLoadingManager {
	@Redirect(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;trackDeltas()Z"))
	private boolean fireblanket$forceVelocityOff(EntityType<?> instance) {
		if (EntityFilters.isTypeForcedVelocityOff(instance)) {
			return false;
		}

		return instance.trackDeltas();
	}
}
