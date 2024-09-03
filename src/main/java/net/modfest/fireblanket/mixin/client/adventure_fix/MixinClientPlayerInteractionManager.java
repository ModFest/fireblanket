package net.modfest.fireblanket.mixin.client.adventure_fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.modfest.fireblanket.FireblanketConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
	@Inject(
		method = "interactEntity",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", shift = At.Shift.AFTER),
		cancellable = true)
	private void fireblanket$filterEntityInteractByTag(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!player.getAbilities().allowModifyWorld && (
				entity.getType().isIn(FireblanketConstants.ENTITY_INTERACTION_RESTRICTED) ||
				player.getStackInHand(hand).isIn(FireblanketConstants.ITEM_INTERACTION_RESTRICTED))) {
			ci.setReturnValue(ActionResult.FAIL);
			ci.cancel();
		}
	}

	@Inject(
		method = "interactEntityAtLocation",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", shift = At.Shift.AFTER),
		cancellable = true
	)
	private void fireblanket$filterEntityInteractAtLocationByTag(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!player.getAbilities().allowModifyWorld && (
				entity.getType().isIn(FireblanketConstants.ENTITY_INTERACTION_RESTRICTED) ||
				player.getStackInHand(hand).isIn(FireblanketConstants.ITEM_INTERACTION_RESTRICTED))) {
			ci.setReturnValue(ActionResult.FAIL);
			ci.cancel();
		}
	}

	@WrapOperation(
		method = "interactBlockInternal",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;")
	)
	private ItemActionResult fireblanket$filterItemBlockInteractByTag(BlockState blockState, ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ItemActionResult> op) {
 		if (!player.getAbilities().allowModifyWorld) {
			if (stack.isIn(FireblanketConstants.ITEM_INTERACTION_RESTRICTED)) {
				return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
			if (blockState.isIn(FireblanketConstants.BLOCK_INTERACTION_RESTRICTED)) {
				return ItemActionResult.FAIL;
			}
		}
		return op.call(blockState, stack, world, player, hand, hitResult);
	}

	@WrapOperation(
		method = "interactBlockInternal",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;")
	)
	private ActionResult fireblanket$filterBlockInteractByTag(BlockState blockState, World world, PlayerEntity player, BlockHitResult hitResult, Operation<ActionResult> op) {
		if (!player.getAbilities().allowModifyWorld && blockState.isIn(FireblanketConstants.BLOCK_INTERACTION_RESTRICTED)) {
			return ActionResult.FAIL;
		}
		return op.call(blockState, world, player, hitResult);
	}

	@Inject(
		method = "attackEntity",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", shift = At.Shift.AFTER),
		cancellable = true
	)
	private void fireblanket$filterAttackEntityByTag(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (!player.getAbilities().allowModifyWorld && target.getType().isIn(FireblanketConstants.ENTITY_ATTACK_RESTRICTED)) {
			ci.cancel();
		}
	}
}
