package net.modfest.fireblanket.client.render;

import com.mojang.logging.LogUtils;
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
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * @author Ampflower
 **/
public final class BlockTimingRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final Logger logger = LogUtils.getLogger();

	private static final Set<Object> witnesses = Collections.newSetFromMap(new WeakHashMap<>());

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

			if (pos == null) {
				// TODO: implement recursive object dumper in forbidden stack walker
				//  This needs to be fixed properly but this is a debugger,
				//  it can have a lil' bit of jank as a treat.
				// Disable the snitch call because it's utterly useless with Lithium,
				// as it wraps all objects, without providing a toString.
//				if (witnesses.add(ticker)) {
//					logger.warn("Ticker {} ({}) has a missing block position?!?!?!", ticker, ticker.getClass().getSimpleName());
//				}
				continue;
			}

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
