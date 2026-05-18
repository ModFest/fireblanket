package net.modfest.fireblanket.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Previously used to hide signs used for PictureSign.
 * <p>
 * Unless we ever bring it back, this can sit dormant.
 */
// priority to dodge PictureSign
@Deprecated(forRemoval = true)
@Mixin(value = AbstractSignRenderer.class, priority = 5000)
public class MixinSignBlockEntityRenderer {
	@Inject(
		at = @At("HEAD"),
		method = {
			"submit(Lnet/minecraft/client/renderer/blockentity/state/SignRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
		}, cancellable = true)
	public void fireblanket$DontRenderHiddenSigns(CallbackInfo ci, @Local(argsOnly = true) SignRenderState state) {
		if (state.lightCoords == 0) {
			ci.cancel();
		}
	}
}
