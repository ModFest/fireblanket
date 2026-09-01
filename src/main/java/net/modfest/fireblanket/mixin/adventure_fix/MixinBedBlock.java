package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class MixinBedBlock {
	@Inject(method = "destroyOnUse", at = @At("HEAD"), cancellable = true)
	private void whyDoesThisStillExist(CallbackInfoReturnable<InteractionResult> ci) {
		ci.setReturnValue(InteractionResult.CONSUME);
	}
}
