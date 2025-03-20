package net.modfest.fireblanket.mixin.client.render_verification;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixin.accessor.MatrixStackAccessor;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	@Unique
	private volatile Entity fireblanket$verify;
	@Unique
	private volatile int fireblanket$depth;

	@Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
	private <T extends Entity> void fireblanket$verifyStart(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (FireblanketClient.VERIFY_RENDER) {
			fireblanket$verify = entity;
			fireblanket$depth = ((MatrixStackAccessor)matrices).getStack().size();
		}
	}

	@Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
	private <T extends Entity> void fireblanket$verifyEnd(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (FireblanketClient.VERIFY_RENDER && fireblanket$verify == entity) {
			int size = ((MatrixStackAccessor) matrices).getStack().size();
			if (fireblanket$depth != size) {
				throw new IllegalStateException("Entity " + entity.getClass() + " had different matrix depths before and after rendering: should be " + fireblanket$verify + " but was " + size);
			}
		}
	}
}
