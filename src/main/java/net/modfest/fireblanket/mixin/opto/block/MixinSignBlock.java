package net.modfest.fireblanket.mixin.opto.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The ticker in the signs only do work on the server.
 * No point in iterating over them in the client, it just wastes cycles.
 * <p>
 * Micro-optimisation. Bonus points if you made the ticker sleepy with Lithium or so.
 *
 * @author Ampflower
 **/
@Mixin({SignBlock.class, WallHangingSignBlock.class, CeilingHangingSignBlock.class})
public class MixinSignBlock {
	@Inject(
		method = "getTicker",
		at = @At("HEAD"),
		cancellable = true
	)
	private void forceServerOnly(
		final CallbackInfoReturnable<BlockEntityTicker<?>> cir,
		final @Local(argsOnly = true) Level level
	) {
		if (level.isClientSide()) {
			cir.setReturnValue(null);
		}
	}
}
