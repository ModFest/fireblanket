package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class MixinFarmlandBlock {
	@Inject(
		method = "setToDirt",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void doNotSetToDirt(@Nullable Entity entity, BlockState state, World world, BlockPos pos, CallbackInfo ci) {
		if (entity instanceof PlayerEntity player && !player.canModifyBlocks()) {
			ci.cancel();
		}
	}
}
