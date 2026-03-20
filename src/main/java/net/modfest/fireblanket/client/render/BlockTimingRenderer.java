package net.modfest.fireblanket.client.render;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.mixin.accessor.AccessorLevel;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;

/**
 * @author Ampflower
 **/
public final class BlockTimingRenderer implements DebugRenderer.SimpleDebugRenderer {

	private final Minecraft minecraft;

	public BlockTimingRenderer(final Minecraft minecraft) {
		this.minecraft = minecraft;
	}

	@Override
	public void emitGizmos(
		final double camX,
		final double camY,
		final double camZ,
		final DebugValueAccess debugValues,
		final Frustum frustum,
		final float partialTicks
	) {
		final ClientLevel level = this.minecraft.level;

		if (level == null) {
			return;
		}

		for (final TickingBlockEntity ticker : ((AccessorLevel) level).getBlockEntityTickers()) {
			final BlockPos pos = ticker.getPos();

			if (!frustum.isVisible(new AABB(pos))) {
				continue;
			}

			final BlockEntity entity = level.getBlockEntity(pos);

			if (!(entity instanceof ObservableTicks observed)) {
				continue;
			}

			long t = observed.fireblanket$getTickTime();
			String s;
			if (t > 1000) {
				t /= 1000;
				s = t + " μs";
			} else {
				s = t + " ns";
			}

			final GizmoProperties gizmo = Gizmos.billboardText(s, Vec3.upFromBottomCenterOf(pos, 1.3d), TextGizmo.Style.whiteAndCentered());

			if (SharedConstants.DEBUG_ENABLED) {
				gizmo.setAlwaysOnTop();
			}
		}
	}
}
