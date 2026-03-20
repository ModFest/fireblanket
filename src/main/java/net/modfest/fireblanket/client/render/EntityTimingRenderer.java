package net.modfest.fireblanket.client.render;

import net.minecraft.SharedConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;

/**
 * @author Ampflower
 **/
public final class EntityTimingRenderer implements DebugRenderer.SimpleDebugRenderer {

	private final Minecraft minecraft;

	public EntityTimingRenderer(final Minecraft minecraft) {
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

		for (final Entity entity : level.entitiesForRendering()) {
			if (entity == this.minecraft.getCameraEntity() && this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON) {
				continue;
			}

			if ((!SharedConstants.DEBUG_ENABLED && entity.isInvisible()) || !frustum.isVisible(entity.getBoundingBox())) {
				continue;
			}

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

			final float entityPartialTicks = this.minecraft.getDeltaTracker()
				.getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));

			final Vec3 textPosition = entity.getPosition(entityPartialTicks)
				.add(0.d, entity.getBbHeight() + 0.8d, 0.d);

			final GizmoProperties gizmo = Gizmos.billboardText(s, textPosition, TextGizmo.Style.whiteAndCentered());

			if (SharedConstants.DEBUG_ENABLED) {
				gizmo.setAlwaysOnTop();
			}
		}
	}
}
