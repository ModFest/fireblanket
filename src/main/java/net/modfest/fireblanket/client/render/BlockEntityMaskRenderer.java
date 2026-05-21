package net.modfest.fireblanket.client.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.modfest.fireblanket.FireblanketMixin;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixin.client.accessor.AccessorClientChunkCache;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author Ampflower
 **/
public final class BlockEntityMaskRenderer implements DebugRenderer.SimpleDebugRenderer {

	private static final boolean alwaysVisible = FireblanketMixin.DO_MASKING | SharedConstants.DEBUG_ENABLED;

	private final Minecraft minecraft;

	public BlockEntityMaskRenderer(final Minecraft minecraft) {
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
		if (ClientState.MASKED_BERS.isEmpty()) {
			return;
		}

		final ClientLevel level = this.minecraft.level;

		if (level == null) {
			return;
		}

		final var clientChunkCache = (AccessorClientChunkCache) level.getChunkSource();

		if (clientChunkCache == null) {
			return;
		}

		final var storage = (AccessorClientChunkCache.Storage) (Object) (clientChunkCache).getStorage();

		if (storage == null) {
			return;
		}

		final AtomicReferenceArray<@Nullable LevelChunk> chunks = storage.getChunks();

		if (chunks == null) {
			return;
		}

		for (int i = 0; i < chunks.length(); i++) {
			final LevelChunk chunk = chunks.get(i);

			if (chunk == null || chunk.isEmpty()) {
				continue;
			}

			final AABB chunkBounds;
			{
				final ChunkPos pos = chunk.getPos();
				final BlockPos a = pos.getBlockAt(0, chunk.getMinY(), 0);
				final BlockPos b = pos.getBlockAt(15, chunk.getMaxY(), 15);
				chunkBounds = AABB.encapsulatingFullBlocks(a, b);
			}

			if (!frustum.isVisible(chunkBounds)) {
				continue;
			}

			if (chunk.getBlockEntities() instanceof Object2ObjectMap<BlockPos, BlockEntity> map) {
				iterate(Object2ObjectMaps.fastIterator(map), frustum);
			} else {
				iterate(chunk.getBlockEntities().entrySet().iterator(), frustum);
			}
		}
	}

	private static void iterate(
		final Iterator<? extends Map.Entry<BlockPos, BlockEntity>> itr,
		final Frustum frustum
	) {
		while (itr.hasNext()) {
			final var entry = itr.next();
			final BlockPos pos = entry.getKey();
			final BlockEntity entity = entry.getValue();
			final AABB box = new AABB(pos);

			if (!frustum.isVisible(box)) {
				continue;
			}

			if (!ClientState.MASKED_BERS.contains(entity.getType())) {
				continue;
			}

			final GizmoProperties gizmo = Gizmos.cuboid(
				box,
				GizmoStyle.stroke(ARGB.color(192, 200, 220, 40))
			);

			if (alwaysVisible) {
				gizmo.setAlwaysOnTop();
			}
		}
	}
}
