package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerInteractionManager {
	@WrapOperation(
		method = "useItemOn",
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
		method = "useItemOn",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;useWithoutItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;")
	)
	private InteractionResult fireblanket$filterBlockInteractByTag(BlockState blockState, Level world, Player player, BlockHitResult hitResult, Operation<InteractionResult> op) {
		if (!player.getAbilities().mayBuild && InteractionCheck.preventUseBlock(player, blockState)) {
			return InteractionResult.FAIL;
		}
		return op.call(blockState, world, player, hitResult);
	}
}
