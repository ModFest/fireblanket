package net.modfest.fireblanket.mixin.client.render_verification;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
//	@Unique
//	private WeakReference<Entity> fireblanket$verify;
//	@Unique
//	private volatile int fireblanket$depth;
//
//	@Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
//	private <T extends Entity> void fireblanket$verifyStart(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
//		if (FireblanketClient.VERIFY_RENDER) {
//			fireblanket$verify = new WeakReference<>(entity);
//			fireblanket$depth = ((MatrixStackAccessor)matrices).getStack().size();
//		}
//	}
//
//	@Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
//	private <T extends Entity> void fireblanket$verifyEnd(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
//		if (FireblanketClient.VERIFY_RENDER && fireblanket$verify.get() == entity) {
//			int size = ((MatrixStackAccessor) matrices).getStack().size();
//			if (fireblanket$depth != size) {
//				throw new IllegalStateException("Entity " + entity.getClass() + " had different matrix depths before and after rendering: should be " + fireblanket$verify + " but was " + size);
//			}
//		}
//
//		fireblanket$verify = new WeakReference<>(null);
//	}
}
