package net.modfest.fireblanket.mixin.adventure_fix;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(FlowerPotBlock.class)
public class MixinFlowerPotBlock {

	@Inject(at=@At("HEAD"), method="onUse", cancellable=true)
	public void fireblanket$noStealPotsInAdventure(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit,
			CallbackInfoReturnable<ActionResult> ci) {
		if (!player.canModifyBlocks()) {
			ci.setReturnValue(ActionResult.FAIL);
		}
	}
	
}
