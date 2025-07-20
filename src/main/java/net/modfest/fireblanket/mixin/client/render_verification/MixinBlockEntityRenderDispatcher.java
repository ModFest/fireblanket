package net.modfest.fireblanket.mixin.client.render_verification;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.mixin.accessor.MatrixStackAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
//	@Unique
//	private volatile WeakReference<BlockEntity> fireblanket$verify;
//	@Unique
//	private volatile int fireblanket$depth;
//
//	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"))
//	private <T extends BlockEntity> void fireblanket$verifyStart(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
//		if (FireblanketClient.VERIFY_RENDER) {
//			fireblanket$verify = new WeakReference<>(blockEntity);
//			fireblanket$depth = ((MatrixStackAccessor)matrices).getStack().size();
//		}
//	}
//
//	@Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"))
//	private <T extends BlockEntity> void fireblanket$verifyEnd(T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
//		if (FireblanketClient.VERIFY_RENDER && fireblanket$verify.get() == blockEntity) {
//			int size = ((MatrixStackAccessor) matrices).getStack().size();
//			if (fireblanket$depth != size) {
//				throw new IllegalStateException("Block entity " + blockEntity.getClass() + " had different matrix depths before and after rendering: should be " + fireblanket$verify + " but was " + size);
//			}
//		}
//
//		fireblanket$verify = new WeakReference<>(null);
//	}
}
