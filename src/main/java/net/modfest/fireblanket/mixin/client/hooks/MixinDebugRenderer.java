package net.modfest.fireblanket.mixin.client.hooks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.culling.Frustum;
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
	// FIXME: just integrate this into the vanilla debug stack.
	//  The renderer in question is already built for it.
	@Inject(method = "emitGizmos", at = @At("RETURN"))
	private void onRenderLate(final Frustum frustum, final double camX, final double camY, final double camZ, final float partialTicks, final CallbackInfo ci) {
		RenderRegionRenderer.instance.emitGizmos(camX, camY, camZ, /* unused */ null, frustum, partialTicks);
	}
}
