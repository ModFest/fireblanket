package net.modfest.fireblanket.mixin.entity_immutability;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(EntityType.class)
public class MixinEntityType {
	@WrapMethod(
		method = "create(Lnet/minecraft/server/world/ServerWorld;Ljava/util/function/Consumer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/SpawnReason;ZZ)Lnet/minecraft/entity/Entity;"
	)
	private <T extends Entity> T afterCreate(ServerWorld world, @Nullable Consumer<T> afterConsumer, BlockPos pos, SpawnReason reason, boolean alignPosition, boolean invertY, Operation<T> original) {
		Consumer<T> processImmutable = entity -> {
			if (entity.getWorld() instanceof ServerWorld serverWorld && serverWorld.getGameRules().getBoolean(Fireblanket.NEW_ENTITIES_IMMUTABLE) && reason == SpawnReason.SPAWN_ITEM_USE){
				ImmutableEntities.makeImmutable(entity);
			}
		};
		afterConsumer = afterConsumer == null ? processImmutable : afterConsumer.andThen(processImmutable);
		return original.call(world, afterConsumer, pos, reason, alignPosition, invertY);
	}
}
