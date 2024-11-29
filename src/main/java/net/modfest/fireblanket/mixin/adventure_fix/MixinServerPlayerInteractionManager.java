package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.modfest.fireblanket.FireblanketConstants;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {
	@WrapOperation(
		method = "interactBlock",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;")
	)
	private ItemActionResult fireblanket$filterItemBlockInteractByTag(BlockState blockState, ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ItemActionResult> op) {
		if (!player.getAbilities().allowModifyWorld) {
			if (InteractionCheck.preventUseItem(player, stack)) {
				return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			}
			if (InteractionCheck.preventUseBlock(player, blockState)) {
				return ItemActionResult.FAIL;
			}
		}
		return op.call(blockState, stack, world, player, hand, hitResult);
	}

	@WrapOperation(
		method = "interactBlock",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;")
	)
	private ActionResult fireblanket$filterBlockInteractByTag(BlockState blockState, World world, PlayerEntity player, BlockHitResult hitResult, Operation<ActionResult> op) {
		if (!player.getAbilities().allowModifyWorld && InteractionCheck.preventUseBlock(player, blockState)) {
			return ActionResult.FAIL;
		}
		return op.call(blockState, world, player, hitResult);
	}
}
