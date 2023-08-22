package net.modfest.fireblanket.mixin.client.be_masking;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.client.render.QuadEmitter;
import net.modfest.fireblanket.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"))
	private <T extends BlockEntity> void fireblanket$renderMaskedBlockEntities(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
		if (ClientState.MASKED_BERS.contains(blockEntity.getType())) {
			VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.TRANSLUCENT_BROKEN_DEPTH);

			QuadEmitter.buildBox(buffer, matrices, 0, 1.00001f, 0, 1.00001f, 0, 1.00001f, 30, 200, 220, 40);

			// Please let this be a normal field trip
			if (vertexConsumers instanceof VertexConsumerProvider.Immediate imm) {
				imm.draw();
			}
		}
	}
}
