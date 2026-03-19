package net.modfest.fireblanket.mixin.entity_immutability;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(EntityType.class)
public class MixinEntityType {
	@WrapMethod(
		method = "create(Lnet/minecraft/server/level/ServerLevel;Ljava/util/function/Consumer;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/EntitySpawnReason;ZZ)Lnet/minecraft/world/entity/Entity;"
	)
	private <T extends Entity> T afterCreate(ServerLevel world, @Nullable Consumer<T> afterConsumer, BlockPos pos, EntitySpawnReason reason, boolean alignPosition, boolean invertY, Operation<T> original) {
		Consumer<T> processImmutable = entity -> {
			if (entity.level() instanceof ServerLevel serverWorld && serverWorld.getGameRules().get(Fireblanket.NEW_ENTITIES_IMMUTABLE) && reason == EntitySpawnReason.SPAWN_ITEM_USE) {
				ImmutableEntities.makeImmutable(entity);
			}
		};
		afterConsumer = afterConsumer == null ? processImmutable : afterConsumer.andThen(processImmutable);
		return original.call(world, afterConsumer, pos, reason, alignPosition, invertY);
	}
}
