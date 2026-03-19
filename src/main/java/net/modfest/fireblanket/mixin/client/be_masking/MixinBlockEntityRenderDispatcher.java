package net.modfest.fireblanket.mixin.client.be_masking;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.client.ClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	@Inject(method = "tryExtractRenderState", at = @At("HEAD"))
	private <T extends BlockEntity> void fireblanket$renderMaskedBlockEntities(CallbackInfoReturnable<?> ci, @Local T blockEntity) {
		if (ClientState.MASKED_BERS.contains(blockEntity.getType())) {
			Gizmos.cuboid(blockEntity.getBlockPos(), 0.01F, GizmoStyle.stroke(0xFF000000 | (200 << 16) | (220 << 8) | 40, 1.F))
				.setAlwaysOnTop();
		}
	}
}
