package net.modfest.fireblanket.mixin.entity_immutability;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.modfest.fireblanket.mixinsupport.EntityWithReason;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(EntityType.class)
public class MixinEntityType {
	@ModifyReturnValue(method = "appendCustomEntityStackConfig", at = @At("RETURN"))
	private static <T extends Entity> Consumer<T> addImmutable(
		final Consumer<T> original,
		final @Local(argsOnly = true) @Nullable LivingEntity user
	) {
		if (!(user instanceof ServerPlayer player)) {
			return original;
		}

		return original.andThen(entity -> ImmutableEntities.makeImmutable(player, entity));
	}

	@ModifyReturnValue(
		method = "create(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnReason;)Lnet/minecraft/world/entity/Entity;",
		at = @At("RETURN")
	)
	private <T extends Entity> T tagReasonToEntity(
		final T entity,
		final @Local(argsOnly = true) EntitySpawnReason reason
	) {
		if (entity instanceof EntityWithReason reasonableEntity) {
			reasonableEntity.fireblanket$setReason(reason);
		}

		return entity;
	}
}
