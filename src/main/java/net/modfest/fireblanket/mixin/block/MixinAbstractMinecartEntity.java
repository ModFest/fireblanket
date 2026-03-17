package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
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
@Mixin(AbstractMinecart.class)
public class MixinAbstractMinecartEntity {

	@Inject(
		method = "createMinecart",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
			shift = At.Shift.AFTER
		),
		slice = @Slice(from = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/EntityType;createDefaultStackConfig(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/function/Consumer;"
		))
	)
	private static void fireblanket$onCreateMinecart(
		final CallbackInfoReturnable<AbstractMinecart> ci,
		final @Local(argsOnly = true) Level world,
		final @Local(argsOnly = true) Player player,
		final @Local AbstractMinecart minecartEntity
	) {
		if (world.isClientSide) {
			return;
		}

		if (!(minecartEntity instanceof CommandBE cbe)) {
			return;
		}

		// TODO: ban cart if player == null?
		//  Currently opting to clear the last set if player's unknown.
		UUID uuid = null;
		if (player != null) {
			uuid = player.getUUID();
		}

		cbe.fireblanket$setOwner(uuid);
		cbe.fireblanket$setLastUpdate(uuid);
	}
}
