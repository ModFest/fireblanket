package net.modfest.fireblanket.mixin.client.timing;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	@Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At("HEAD"))
	private <T extends BlockEntity> void fireblanket$beTiming(T blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo ci) {
		if (ClientState.displayTickTimes && blockEntity instanceof ObservableTicks observed) {
			BlockPos pos = blockEntity.getBlockPos();

			long t = observed.fireblanket$getTickTime();
			String s;
			int color = 0xFFFFFF;
			if (t > 1000) {
				t /= 1000;
				s = t + " us";
			} else {
				s = t + " ns";
			}

			DebugRenderer.renderFloatingText(matrices, vertexConsumers, s,
				pos.getX() + 0.5,
				pos.getY() + 0.5,
				pos.getZ() + 0.5,
				color, 0.03F, true, 0, true);
		}
	}
}
