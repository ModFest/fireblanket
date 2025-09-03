package net.modfest.fireblanket.mixin.client.hooks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(DebugRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinDebugRenderer {
	@Inject(method = "renderLate", at = @At("RETURN"))
	private void onRenderLate(MatrixStack stack, VertexConsumerProvider.Immediate imm, double cx, double cy, double cz, CallbackInfo ci) {
		RenderRegionRenderer.instance.render(stack, imm, cx, cy, cz);
	}
}
