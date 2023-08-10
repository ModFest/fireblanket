package net.modfest.fireblanket.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;

// priority to dodge PictureSign
@Mixin(value=SignBlockEntityRenderer.class, priority=5000)
public class MixinSignBlockEntityRenderer {

	@Inject(at=@At("HEAD"), method="render", cancellable=true)
	public void fireblanket$DontRenderHiddenSigns(SignBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		if (light == 0) ci.cancel();
	}
	
}
