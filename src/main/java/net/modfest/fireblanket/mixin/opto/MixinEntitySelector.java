package net.modfest.fireblanket.mixin.opto;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class MixinEntitySelector {
	@Shadow
	protected abstract void checkPermissions(CommandSourceStack source) throws CommandSyntaxException;

	@Shadow
	@Final
	private @Nullable String playerName;

	@Shadow
	@Final
	private @Nullable UUID entityUUID;

	@Shadow
	@Final
	private Function<Vec3, Vec3> position;

	@Shadow
	protected abstract Predicate<Entity> getPredicate(Vec3 pos, @Nullable AABB box, @Nullable FeatureFlagSet enabledFeatures);

	@Shadow
	@Final
	private boolean currentEntity;

	@Shadow
	protected abstract int getResultLimit();

	@Shadow
	public abstract boolean isWorldLimited();

	@Shadow
	protected abstract <T extends Entity> List<T> sortAndLimit(Vec3 pos, List<T> entities);

	@Shadow
	@Final
	private MinMaxBounds.Doubles range;

	@Shadow
	@Final
	private @Nullable AABB aabb;

	@Shadow
	@Nullable
	protected abstract AABB getAbsoluteAabb(Vec3 offset);

	@Shadow
	@Final
	private List<Predicate<Entity>> contextFreePredicates;

	/**
	 * @author Jasmine
	 * @reason Always predicate on distance and d(x|y|z) *before* checking the NBT, or any other predicate.
	 */
	@Overwrite
	public List<ServerPlayer> findPlayers(CommandSourceStack source) throws CommandSyntaxException {
		this.checkPermissions(source);
		if (this.playerName != null) {
			ServerPlayer serverPlayerEntity = source.getServer().getPlayerList().getPlayerByName(this.playerName);
			return serverPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(serverPlayerEntity);
		} else if (this.entityUUID != null) {
			ServerPlayer serverPlayerEntity = source.getServer().getPlayerList().getPlayer(this.entityUUID);
			return serverPlayerEntity == null ? Collections.emptyList() : Lists.newArrayList(serverPlayerEntity);
		} else {
			Vec3 pos = this.position.apply(source.getPosition());
			AABB box = this.getAbsoluteAabb(pos);
			Predicate<Entity> predicate = this.getPredicate(pos, box, null);
			if (this.currentEntity) {
				if (source.getEntity() instanceof ServerPlayer serverPlayerEntity2 && predicate.test(serverPlayerEntity2)) {
					return Lists.newArrayList(serverPlayerEntity2);
				}

				return Collections.emptyList();
			} else {
				int i = this.getResultLimit();
				List<ServerPlayer> list;
				if (this.isWorldLimited()) {
					// The change is here: Get players with distance predicate first, move onto base predicate later.
					predicate = getPositionOnlyPredicate(pos, box, null);

					Predicate<Entity> basePredicate = Util.allOf(this.contextFreePredicates);

					list = source.getLevel().getPlayers(predicate, i);
					list.removeIf(basePredicate.negate());
				} else {
					list = Lists.newArrayList();

					for (ServerPlayer serverPlayerEntity3 : source.getServer().getPlayerList().getPlayers()) {
						if (predicate.test(serverPlayerEntity3)) {
							list.add(serverPlayerEntity3);
							if (list.size() >= i) {
								return list;
							}
						}
					}
				}

				return this.sortAndLimit(pos, list);
			}
		}
	}

	@Unique
	private Predicate<Entity> getPositionOnlyPredicate(Vec3 pos) {
		Predicate<Entity> predicate = e -> true;
		if (this.aabb != null) {
			AABB box = this.aabb.move(pos);
			predicate = predicate.and(entity -> box.intersects(entity.getBoundingBox()));
		}

		if (!this.range.isAny()) {
			predicate = predicate.and(entity -> this.range.matchesSqr(entity.distanceToSqr(pos)));
		}

		return predicate;
	}

	@Unique
	private Predicate<Entity> getPositionOnlyPredicate(Vec3 pos, @Nullable AABB box, @Nullable FeatureFlagSet enabledFeatures) {
		boolean bl = enabledFeatures != null;
		boolean bl2 = box != null;
		boolean bl3 = this.range != null && !this.range.isAny();
		int i = (bl ? 1 : 0) + (bl2 ? 1 : 0) + (bl3 ? 1 : 0);
		List<Predicate<Entity>> list;
		if (i == 0) {
			list = new ArrayList<>();
		} else {
			List<Predicate<Entity>> list2 = new ObjectArrayList<>(3);
			if (bl) {
				list2.add(entity -> entity.getType().isEnabled(enabledFeatures));
			}

			if (bl2) {
				list2.add(entity -> box.intersects(entity.getBoundingBox()));
			}

			if (bl3) {
				list2.add(entity -> this.range.matchesSqr(entity.distanceToSqr(pos)));
			}

			list = list2;
		}

		return Util.allOf(list);
	}
}
