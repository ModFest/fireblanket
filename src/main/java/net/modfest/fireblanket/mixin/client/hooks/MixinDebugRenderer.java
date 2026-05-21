package net.modfest.fireblanket.mixin.client.hooks;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.modfest.fireblanket.FireblanketMixin;
import net.modfest.fireblanket.client.FireblanketDebug;
import net.modfest.fireblanket.client.render.BlockEntityMaskRenderer;
import net.modfest.fireblanket.client.render.BlockTimingRenderer;
import net.modfest.fireblanket.client.render.EntityMaskRenderer;
import net.modfest.fireblanket.client.render.EntityTimingRenderer;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * @author Ampflower
 **/
@Mixin(DebugRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinDebugRenderer {

	@Shadow
	@Final
	private List<DebugRenderer.SimpleDebugRenderer> renderers;

	@Inject(method = "refreshRendererList", at = @At("TAIL"))
	private void injectFireblanketDebuggers(final CallbackInfo ci, final @Local Minecraft minecraft) {
		if (minecraft.debugEntries.isCurrentlyEnabled(FireblanketDebug.RENDER_REGION_VISUALIZE)) {
			this.renderers.add(RenderRegionRenderer.instance);
		}

		if (minecraft.debugEntries.isCurrentlyEnabled(FireblanketDebug.ENTITY_TICK_TIMES)) {
			this.renderers.add(new EntityTimingRenderer(minecraft));
		}

		if (minecraft.debugEntries.isCurrentlyEnabled(FireblanketDebug.BLOCK_TICK_TIMES)) {
			this.renderers.add(new BlockTimingRenderer(minecraft));
		}

		if (FireblanketMixin.DO_MASKING) {
			this.renderers.add(new EntityMaskRenderer(minecraft));
			this.renderers.add(new BlockEntityMaskRenderer(minecraft));
		}
	}
}
