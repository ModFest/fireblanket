package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public class MixinFarmlandBlock {
	@Inject(
		method = "turnToDirt",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void doNotSetToDirt(@Nullable Entity entity, BlockState state, Level world, BlockPos pos, CallbackInfo ci) {
		if (entity instanceof Player player && !player.mayBuild()) {
			ci.cancel();
		}
	}
}
