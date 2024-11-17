package net.modfest.fireblanket.mixin.entity_immutability;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(EntityType.class)
public class MixinEntityType {
	@WrapOperation(method = "create(Lnet/minecraft/server/world/ServerWorld;Ljava/util/function/Consumer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"))
	private Entity afterCreated(EntityType<?> instance, World world, Operation<Entity> original, ServerWorld world2, @Nullable Consumer<Entity> afterConsumer, BlockPos pos, SpawnReason reason, boolean alignPosition, boolean invertY) {
		var entity = original.call(instance, world);
		if (world.getGameRules().getBoolean(Fireblanket.NEW_ENTITIES_IMMUTABLE) && reason == SpawnReason.SPAWN_EGG) {
			if (entity != null) {
				ImmutableEntities.makeImmutable(entity);
			}
		}
		return entity;
	}
}
