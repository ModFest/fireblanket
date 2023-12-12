package net.modfest.fireblanket.mixin.client.hooks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V", shift = At.Shift.AFTER))
	private void fireblanket$renderHooks(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f projectionMatrix, CallbackInfo ci) {
		MatrixStack mv = RenderSystem.getModelViewStack();
		mv.push(); mv.loadIdentity();
		RenderSystem.applyModelViewMatrix();

		RenderRegionRenderer.render(matrices, tickDelta);

		mv.pop();
		RenderSystem.applyModelViewMatrix();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupTerrain(Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/Frustum;ZZ)V", shift = At.Shift.BEFORE))
	private void fireblanket$wireframeStart(CallbackInfo ci) {
		if (ClientState.wireframe) {
			RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	private void fireblanket$wireframeEnd(CallbackInfo ci) {
		if (ClientState.wireframe) {
			RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}
}
