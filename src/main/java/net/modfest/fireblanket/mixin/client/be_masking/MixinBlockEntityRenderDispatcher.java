package net.modfest.fireblanket.mixin.client.be_masking;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
//	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"))
//	private <T extends BlockEntity> void fireblanket$renderMaskedBlockEntities(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
//		if (ClientState.MASKED_BERS.contains(blockEntity.getType())) {
//			VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.TRANSLUCENT_BROKEN_DEPTH);
//
//			QuadEmitter.buildBox(buffer, matrices, 0, 1.00001f, 0, 1.00001f, 0, 1.00001f, 30, 200, 220, 40);
//
//			// Please let this be a normal field trip
//			if (vertexConsumers instanceof VertexConsumerProvider.Immediate imm) {
//				imm.draw();
//			}
//		}
//	}
}
