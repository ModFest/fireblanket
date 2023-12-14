package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.client.render.QuadEmitter;
import net.modfest.fireblanket.client.render.RenderLayers;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"))
	private <T extends BlockEntity> void fireblanket$renderMaskedBlockEntities(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (ClientState.displayTickTimes && blockEntity instanceof ObservableTicks observed) {
			BlockPos pos = blockEntity.getPos();

			long t = observed.fireblanket$getTickTime();
			String s;
			int color = 0xFFFFFF;
			if (t > 1000) {
				t /= 1000;
				s = t + " us";
			} else {
				s = t + " ns";
			}

			DebugRenderer.drawString(matrices, vertexConsumers, s,
					pos.getX() + 0.5,
					pos.getY() + 0.5,
					pos.getZ() + 0.5,
					color, 0.03F, true, 0, true);
		}
	}
}
