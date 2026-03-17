package net.modfest.fireblanket.mixin.entity_block;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.modfest.fireblanket.FireblanketConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public abstract class MixinEntityType {
	@Shadow
	public abstract boolean is(TagKey<EntityType<?>> tag);

	@Inject(at = @At("HEAD"), method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;", cancellable = true)
	private void blockDisallowedEntities(Level world, EntitySpawnReason reason, CallbackInfoReturnable<Entity> cir) {
		if (this.is(FireblanketConstants.ENTITY_SUMMON_DISALLOWED)) {
			cir.setReturnValue(null);
		}
	}
}
