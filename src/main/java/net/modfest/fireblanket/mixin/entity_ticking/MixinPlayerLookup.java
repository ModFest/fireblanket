package net.modfest.fireblanket.mixin.entity_ticking;

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.mixin.networking.accessor.EntityTrackerAccessor;
import net.fabricmc.fabric.mixin.networking.accessor.ServerChunkLoadingManagerAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(PlayerLookup.class)
public class MixinPlayerLookup {
	/**
	 * @author jaskarth
	 *
	 * @reason Overwritten for performance
	 */
	@Overwrite
	public static Collection<ServerPlayerEntity> tracking(Entity entity) {
		Objects.requireNonNull(entity, "Entity cannot be null");
		ChunkManager manager = entity.getWorld().getChunkManager();

		if (manager instanceof ServerChunkManager) {
			ServerChunkLoadingManager chunkLoadingManager = ((ServerChunkManager) manager).chunkLoadingManager;
			EntityTrackerAccessor tracker = ((ServerChunkLoadingManagerAccessor) chunkLoadingManager).getEntityTrackers().get(entity.getId());

			// return an immutable collection to guard against accidental removals.
			// Fireblanket: Don't use a stream.
			if (tracker != null) {
				Set<PlayerAssociatedNetworkHandler> tracking = tracker.getPlayersTracking();
				ImmutableSet.Builder<ServerPlayerEntity> builder = ImmutableSet.builderWithExpectedSize(tracking.size());
				for (PlayerAssociatedNetworkHandler h : tracking) {
					builder.add(h.getPlayer());
				}

				return builder.build();
			}

			return Collections.emptySet();
		}

		throw new IllegalArgumentException("Only supported on server worlds!");
	}
}
