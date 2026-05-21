package net.modfest.fireblanket.client.render;

import net.minecraft.SharedConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.FireblanketMixin;
import net.modfest.fireblanket.client.ClientState;

/**
 * @author Ampflower
 **/
public final class EntityMaskRenderer implements DebugRenderer.SimpleDebugRenderer {

	private static final boolean alwaysVisible = FireblanketMixin.DO_MASKING | SharedConstants.DEBUG_ENABLED;

	private final Minecraft minecraft;

	public EntityMaskRenderer(final Minecraft minecraft) {
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
		if (ClientState.MASKED_ENTITIES.isEmpty()) {
			return;
		}

		final ClientLevel level = this.minecraft.level;

		if (level == null) {
			return;
		}

		for (final Entity entity : level.entitiesForRendering()) {
			if (entity == this.minecraft.getCameraEntity() && this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON) {
				continue;
			}

			if ((!alwaysVisible && entity.isInvisible()) || !frustum.isVisible(entity.getBoundingBox())) {
				continue;
			}

			if (!ClientState.MASKED_ENTITIES.contains(entity.getType())) {
				continue;
			}

			final float entityPartialTicks = this.minecraft.getDeltaTracker()
				.getGameTimeDeltaPartialTick(!level.tickRateManager().isEntityFrozen(entity));

			final Vec3 delta = entity.getPosition(entityPartialTicks).subtract(entity.position());

			final GizmoProperties gizmo = Gizmos.cuboid(
				entity.getBoundingBox().move(delta),
				GizmoStyle.stroke(ARGB.color(192, 200, 220, 40))
			);

			if (alwaysVisible) {
				gizmo.setAlwaysOnTop();
			}
		}
	}
}
