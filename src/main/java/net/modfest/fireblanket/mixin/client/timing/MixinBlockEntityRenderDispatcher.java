package net.modfest.fireblanket.mixin.client.timing;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	@Inject(method = "tryExtractRenderState", at = @At("HEAD"))
	private <T extends BlockEntity> void fireblanket$beTiming(
		CallbackInfoReturnable<?> ci,
		@Local(argsOnly = true) T blockEntity
	) {
		if (ClientState.displayTickTimes && blockEntity instanceof ObservableTicks observed) {
			BlockPos pos = blockEntity.getBlockPos();

			long t = observed.fireblanket$getTickTime();
			String s;
			int color = 0xFFFFFFFF;
			if (t > 1000) {
				t /= 1000;
				s = t + " us";
			} else {
				s = t + " ns";
			}

			Gizmos.billboardTextOverBlock(s, pos, 0, color, 0.25F);
		}
	}
}
