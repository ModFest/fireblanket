package net.modfest.fireblanket.mixin.client.hooks;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
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
	@Inject(method = "renderAfterTranslucents", at = @At("RETURN"))
	private void onRenderLate(PoseStack stack, MultiBufferSource.BufferSource imm, double cx, double cy, double cz, CallbackInfo ci) {
		RenderRegionRenderer.instance.render(stack, imm, cx, cy, cz);
	}
}
