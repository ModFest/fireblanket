package net.modfest.fireblanket.mixin.entity_ticking;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.mixin.networking.accessor.EntityTrackerAccessor;
import net.fabricmc.fabric.mixin.networking.accessor.ServerChunkLoadingManagerAccessor;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.ChunkSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

@Mixin(PlayerLookup.class)
public class MixinPlayerLookup {
	/**
	 * @author jaskarth
	 *
	 * @reason Overwritten for performance
	 */
	@Overwrite
	public static Collection<ServerPlayer> tracking(Entity entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		ChunkSource manager = entity.level().getChunkSource();

		if (manager instanceof ServerChunkCache) {
			ChunkMap chunkLoadingManager = ((ServerChunkCache) manager).chunkMap;
			EntityTrackerAccessor tracker = ((ServerChunkLoadingManagerAccessor) chunkLoadingManager).getEntityTrackers().get(entity.getId());

			// return an immutable collection to guard against accidental removals.
			// Fireblanket: Don't use a stream.
			if (tracker != null) {
				Set<ServerPlayerConnection> tracking = tracker.getPlayersTracking();
				ImmutableSet.Builder<ServerPlayer> builder = ImmutableSet.builderWithExpectedSize(tracking.size());
				for (ServerPlayerConnection h : tracking) {
					builder.add(h.getPlayer());
				}

				return builder.build();
			}

			return Collections.emptySet();
		}

		throw new IllegalArgumentException("Only supported on server worlds!");
	}
}
