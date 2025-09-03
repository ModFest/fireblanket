package net.modfest.fireblanket.client.render;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShapes;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.world.render_regions.RenderRegion;

import java.util.Locale;
import java.util.Map;

public final class RenderRegionRenderer implements DebugRenderer.Renderer {
	public static final RenderRegionRenderer instance = new RenderRegionRenderer();

	public static boolean shouldRenderBox = false;
	public static boolean useRegionRenderer = true;

	@Override
	public void render(
		final MatrixStack matrices,
		final VertexConsumerProvider imm,
		final double cameraX,
		final double cameraY,
		final double cameraZ
	) {
		if (!shouldRenderBox) {
			return;
		}

		for (Map.Entry<String, RenderRegion> e : FireblanketClient.renderRegions.getRegionsByName().entrySet()) {
			RenderRegion rr = e.getValue();
			float minX = (float) (rr.minX() - cameraX);
			float minY = (float) (rr.minY() - cameraY);
			float minZ = (float) (rr.minZ() - cameraZ);

			float maxX = (float) (rr.maxX() + 1 - cameraX);
			float maxY = (float) (rr.maxY() + 1 - cameraY);
			float maxZ = (float) (rr.maxZ() + 1 - cameraZ);

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

			DebugRenderer.drawVoxelShapeOutlines(
				matrices, imm.getBuffer(RenderLayer.getLines()),
				VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ),
				0, 0, 0,
				fR, fG, fB, 1.f,
				false
			);

			String name = "[" + rr.mode().name().toLowerCase(Locale.ROOT) + "] " + e.getKey();

			double y = rr.mode().ordinal() * 0.5 - 0.5;

			DebugRenderer.drawString(matrices, imm, name,
				rr.minX() + (rr.maxX() - rr.minX()) / 2.0 + 0.5,
				rr.minY() + (rr.maxY() - rr.minY()) / 2.0 + y,
				rr.minZ() + (rr.maxZ() - rr.minZ()) / 2.0 + 0.5,
				0xFF000000 | (r << 16) | (g << 8) | (b << 0),
				0.03F
			);
		}
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
