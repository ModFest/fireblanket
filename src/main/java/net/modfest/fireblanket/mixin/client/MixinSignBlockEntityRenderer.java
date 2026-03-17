package net.modfest.fireblanket.mixin.client;

import net.minecraft.client.renderer.blockentity.SignRenderer;
import org.spongepowered.asm.mixin.Mixin;

// priority to dodge PictureSign
@Mixin(value = SignRenderer.class, priority = 5000)
public class MixinSignBlockEntityRenderer {

//	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
//	public void fireblanket$DontRenderHiddenSigns(SignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
//		if (light == 0) ci.cancel();
//	}

}
