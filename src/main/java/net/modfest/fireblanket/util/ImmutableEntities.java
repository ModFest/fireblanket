package net.modfest.fireblanket.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.mixin.accessor.ArmorStandEntityAccessor;
import net.modfest.fireblanket.mixin.accessor.ItemFrameAccessor;
import net.modfest.fireblanket.mixinsupport.EntityWithReason;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ImmutableEntities {
	/**
	 * Sets all the relevant immutability flags on the given entity.
	 * <p>
	 * This may include:
	 * <ul>
	 *     <li>{@code NoMovement}, a Fireblanket special to remove movement</li>
	 *     <li>{@code NoGravity}, some mods implement this to mean remove movement</li>
	 *     <li>{@code Fixed}, to mean that its containing stack is immutable</li>
	 * </ul>
	 */
	void fireblanket$setImmutable(boolean immutable);

	static void makeImmutable(
		final UseOnContext context,
		final Entity entity,
		final EntitySpawnReason reason
	) {
		makeImmutable(context.getPlayer(), entity, reason);
	}

	static void makeImmutable(
		final @Nullable Player context,
		final Entity entity
	) {
		final EntitySpawnReason reason;
		if (entity instanceof EntityWithReason reasonableEntity) {
			reason = reasonableEntity.fireblanket$getReason();
		} else {
			reason = null;
		}

		// The method's actually tolerant of it, but there's no way to selectively make it so that
		// outside consumers should always pass the real reason in if possible.
		//noinspection DataFlowIssue - Nullability
		makeImmutable(context, entity, reason);
	}

	static void makeImmutable(
		final @Nullable Player context,
		final Entity entity,
		final EntitySpawnReason reason
	) {
		if (entity instanceof Player) {
			throw new IllegalArgumentException("Players can't be made invulnerable.");
		}
		if (!(context instanceof ServerPlayer player)) {
			return;
		}

		// Sorry adventurers, you can't make immutable entities.
		if (!player.isCreative()) {
			return;
		}

		makeImmutable(player.level(), entity, reason);
	}

	/**
	 * @deprecated Prefer {@link #makeImmutable(Player, Entity, EntitySpawnReason)} when possible.
	 */
	@Deprecated // The deprecation here isn't actually for removal. There's just no better annotation that nags.
	static void makeImmutable(
		final ServerLevel level,
		final Entity entity,
		final EntitySpawnReason reason
	) {
		if (entity instanceof Player) {
			throw new IllegalArgumentException("Players can't be made invulnerable.");
		}
		if (level.getGameRules()
				.get(Fireblanket.NEW_ENTITIES_IMMUTABLE) && reason == EntitySpawnReason.SPAWN_ITEM_USE) {
			ImmutableEntities.makeImmutable(entity);
		}
	}

	/**
	 * @deprecated Prefer {@link #makeImmutable(Player, Entity, EntitySpawnReason)} when possible.
	 */
	@Deprecated // The deprecation here isn't actually for removal. There's just no better annotation that nags.
	static void makeImmutable(Entity entity) {
		// Set invulnerability
		entity.setInvulnerable(true);

		if (entity instanceof ArmorStandEntityAccessor ae) {
			// Disable all slots
			ae.setDisabledSlots(4144959);
			// Disable movement (prevents abuse of fishing rods)
			if (entity instanceof ImmmovableLivingEntity im) {
				im.fireblanket$setNoMovement(true);
			}
		}

		if (entity instanceof ItemFrameAccessor ie) {
			// Make item frames fixed
			ie.setFixed(true);
		}

		if (entity instanceof ImmutableEntities candidate) {
			candidate.fireblanket$setImmutable(true);
		}
	}
}
