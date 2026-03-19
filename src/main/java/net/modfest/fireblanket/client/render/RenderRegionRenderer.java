package net.modfest.fireblanket.client.render;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.Mth;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.world.render_regions.RenderRegion;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public final class RenderRegionRenderer implements DebugRenderer.SimpleDebugRenderer {
	public static final RenderRegionRenderer instance = new RenderRegionRenderer();

	public static boolean shouldRenderBox = false;
	public static boolean useRegionRenderer = true;

	@Override
	public void emitGizmos(
		final double cameraX,
		final double cameraY,
		final double cameraZ,
		// These should be not null, but we don't use these.
		final @Nullable DebugValueAccess valueAccess,
		final @Nullable Frustum frustum,
		final float partialTicks
	) {
		if (!shouldRenderBox) {
			return;
		}

		for (Map.Entry<String, RenderRegion> e : FireblanketClient.renderRegions.getRegionsByName().entrySet()) {
			RenderRegion rr = e.getValue();

			int r = rrRed(rr);
			int g = rrGreen(rr);
			int b = rrBlue(rr);

			int mix = HashCommon.mix(rr.hashCode()) & 0xFFFFFF;

			r = Mth.clamp(r + (((mix >> 16 & 0xFF) - 128) / 8), 0, 255);
			g = Mth.clamp(g + (((mix >> 8 & 0xFF) - 128) / 8), 0, 255);
			b = Mth.clamp(b + (((mix >> 0 & 0xFF) - 128) / 8), 0, 255);

			int argb = 0xFF000000 | (r << 16) | (g << 8) | (b << 0);

			Gizmos.cuboid(
				new AABB(
					rr.minX(), rr.minY(), rr.minZ(),
					rr.maxX() + 1, rr.maxY() + 1, rr.maxZ() + 1
				),
				GizmoStyle.stroke(argb, 1.F),
				false
			);

			String name = "[" + rr.mode().name().toLowerCase(Locale.ROOT) + "] " + e.getKey();

			double y = rr.mode().ordinal() * 0.75 - 0.75;

			Gizmos.billboardText(name,
				new Vec3(
					rr.minX() + (rr.maxX() - rr.minX()) / 2.0 + 0.5,
					rr.minY() + (rr.maxY() - rr.minY()) / 2.0 + y,
					rr.minZ() + (rr.maxZ() - rr.minZ()) / 2.0 + 0.5
				),
				TextGizmo.Style.forColorAndCentered(argb).withScale(1.F)
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
