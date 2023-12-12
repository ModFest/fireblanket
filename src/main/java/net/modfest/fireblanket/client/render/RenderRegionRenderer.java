package net.modfest.fireblanket.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.world.render_regions.RenderRegion;

import java.util.Locale;
import java.util.Map;

public final class RenderRegionRenderer {
	public static boolean shouldRender = false;

	public static void render(MatrixStack matrices, float tickDelta) {
		if (!shouldRender) {
			return;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		Vec3d cam = client.gameRenderer.getCamera().getPos();
		VertexConsumerProvider.Immediate imm = client.getBufferBuilders().getEntityVertexConsumers();

		float oldLw = RenderSystem.getShaderLineWidth();
		RenderSystem.lineWidth(3);

		for (Map.Entry<String, RenderRegion> e : FireblanketClient.renderRegions.getRegionsByName().entrySet()) {
			RenderRegion rr = e.getValue();
			float minX = (float) (rr.minX() - cam.x);
			float minY = (float) (rr.minY() - cam.y);
			float minZ = (float) (rr.minZ() - cam.z);

			float maxX = (float) (rr.maxX() + 1 - cam.x);
			float maxY = (float) (rr.maxY() + 1 - cam.y);
			float maxZ = (float) (rr.maxZ() + 1 - cam.z);

			int r = rrRed(rr);
			int g = rrGreen(rr);
			int b = rrBlue(rr);

			int mix = HashCommon.mix(rr.hashCode()) & 0xFFFFFF;

			r = MathHelper.clamp(r + (((mix >> 16 & 0xFF) - 128) / 8), 0, 255);
			g = MathHelper.clamp(g + (((mix >> 8 & 0xFF) - 128) / 8), 0, 255);
			b = MathHelper.clamp(b + (((mix >> 0 & 0xFF) - 128) / 8), 0, 255);

			float fR = r / 255f;
			float fG = g / 255f;
			float fB = b / 255f;

			// Quads
			QuadEmitter.buildBox(imm.getBuffer(RenderLayers.TRANSLUCENT_BOX), matrices, minX, maxX, minY, maxY, minZ, maxZ, r, g, b, 37);
			// Lines
			WorldRenderer.drawBox(matrices, imm.getBuffer(RenderLayers.LINES_WIDER), minX, minY, minZ, maxX, maxY, maxZ, fR, fG, fB, 1.0f);

			String name = "[" + rr.mode().name().toLowerCase(Locale.ROOT) + "] " + e.getKey();

			DebugRenderer.drawString(matrices, imm, name,
					rr.minX() + (rr.maxX() - rr.minX()) / 2.0 + 0.5,
					rr.minY() + (rr.maxY() - rr.minY()) / 2.0 + 0.5,
					rr.minZ() + (rr.maxZ() - rr.minZ()) / 2.0 + 0.5,
					0xFFFFFF, 0.03F);
		}

		RenderSystem.lineWidth(oldLw);
	}

	private static int rrRed(RenderRegion rr) {
		return switch (rr.mode()) {
			case DENY -> 180;
			case ALLOW -> 40;
			case EXCLUSIVE -> 25;
			default -> 255;
		};
	}

	private static int rrGreen(RenderRegion rr) {
		return switch (rr.mode()) {
			case DENY -> 20;
			case ALLOW -> 210;
			case EXCLUSIVE -> 100;
			default -> 255;
		};
	}

	private static int rrBlue(RenderRegion rr) {
		return switch (rr.mode()) {
			case DENY -> 30;
			case ALLOW -> 20;
			case EXCLUSIVE -> 200;
			default -> 255;
		};
	}
}
