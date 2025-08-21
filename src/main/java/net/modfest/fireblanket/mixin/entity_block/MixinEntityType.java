package net.modfest.fireblanket.mixin.entity_block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;
import net.modfest.fireblanket.FireblanketConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class MixinEntityType {
	@Shadow public abstract boolean isIn(TagKey<EntityType<?>> tag);

	@Inject(at = @At("HEAD"), method = "create(Lnet/minecraft/world/World;Lnet/minecraft/entity/SpawnReason;)Lnet/minecraft/entity/Entity;", cancellable = true)
	private void blockDisallowedEntities(World world, SpawnReason reason, CallbackInfoReturnable<Entity> cir) {
		if (this.isIn(FireblanketConstants.ENTITY_SUMMON_DISALLOWED)) {
			cir.setReturnValue(null);
		}
	}
}
