package net.modfest.fireblanket.mixin.client.adventure_fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteractionManager {
	@Inject(
		method = "interact",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V", shift = At.Shift.AFTER),
		cancellable = true)
	private void fireblanket$filterEntityInteractByTag(
		final Player player,
		final Entity entity,
		final EntityHitResult hitResult,
		final InteractionHand hand,
		final CallbackInfoReturnable<InteractionResult> ci
	) {
		if (!player.getAbilities().mayBuild && (
			InteractionCheck.preventUseEntity(player, entity) ||
				InteractionCheck.preventUseItem(player, player.getItemInHand(hand)))) {
			ci.setReturnValue(InteractionResult.FAIL);
			ci.cancel();
		}
	}

	@WrapOperation(
		method = "performUseItemOn",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
	)
	private InteractionResult fireblanket$filterItemBlockInteractByTag(BlockState blockState, ItemStack stack, Level world, Player player, InteractionHand hand, BlockHitResult hitResult, Operation<InteractionResult> op) {
		if (!player.getAbilities().mayBuild) {
			if (InteractionCheck.preventUseItem(player, stack)) {
				return InteractionResult.TRY_WITH_EMPTY_HAND;
			}
			if (InteractionCheck.preventUseBlock(player, blockState)) {
				return InteractionResult.FAIL;
			}
		}
		return op.call(blockState, stack, world, player, hand, hitResult);
	}

	@WrapOperation(
		method = "performUseItemOn",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useWithoutItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
	)
	private InteractionResult fireblanket$filterBlockInteractByTag(BlockState blockState, Level world, Player player, BlockHitResult hitResult, Operation<InteractionResult> op) {
		if (!player.getAbilities().mayBuild && InteractionCheck.preventUseBlock(player, blockState)) {
			return InteractionResult.FAIL;
		}
		return op.call(blockState, world, player, hitResult);
	}

	@Inject(
		method = "attack",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V", shift = At.Shift.AFTER),
		cancellable = true
	)
	private void fireblanket$filterAttackEntityByTag(Player player, Entity target, CallbackInfo ci) {
		if (!player.getAbilities().mayBuild && InteractionCheck.preventAttackEntity(player, target)) {
			ci.cancel();
		}
	}
}
