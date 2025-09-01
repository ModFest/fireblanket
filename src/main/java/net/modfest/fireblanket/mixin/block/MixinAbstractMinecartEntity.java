package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.world.World;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * @author Ampflower
 **/
@Mixin(AbstractMinecartEntity.class)
public class MixinAbstractMinecartEntity {

	@Inject(
		method = "create",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
			shift = At.Shift.AFTER
		),
		slice = @Slice(from = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/EntityType;copier(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Ljava/util/function/Consumer;"
		))
	)
	private static void fireblanket$onCreateMinecart(
		final CallbackInfoReturnable<AbstractMinecartEntity> ci,
		final @Local(argsOnly = true) World world,
		final @Local(argsOnly = true) PlayerEntity player,
		final @Local AbstractMinecartEntity minecartEntity
	) {
		if (world.isClient) {
			return;
		}

		if (!(minecartEntity instanceof CommandBE cbe)) {
			return;
		}

		// TODO: ban cart if player == null?
		//  Currently opting to clear the last set if player's unknown.
		UUID uuid = null;
		if (player != null) {
			uuid = player.getUuid();
		}

		cbe.fireblanket$setOwner(uuid);
		cbe.fireblanket$setLastUpdate(uuid);
	}
}
