package net.modfest.fireblanket.mixin.mods.hotdognalds;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.modfest.fireblanket.diagnostics.ForbiddenStackWalker;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;
import net.modfest.fireblanket.stacksmash.StackUtil;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Ampflower
 */
@Pseudo
@Mixin(targets = "dev.chililisoup.hotdognalds.entity.FoodEntity")
public abstract class MixinFoodEntity extends Entity implements ImmutableEntities, ImmmovableLivingEntity {
	@Unique
	private static final Class<?> spawnItem = ForbiddenStackWalker.classOrNull(
		"dev.chililisoup.hotdognalds.item.SpawnItem");

	private MixinFoodEntity(final EntityType<?> type, final Level level) {
		super(type, level);
	}

	@Inject(method = "create", at = @At("RETURN"))
	private static void fireblanket$onCreate(
		final CallbackInfoReturnable<@Nullable MixinFoodEntity> cir,
		@Local(argsOnly = true) EntitySpawnReason reason,
		final @Local(argsOnly = true) @Nullable Player player
	) {
		final MixinFoodEntity self = cir.getReturnValue();
		if (self == null) {
			return;
		}

		if (
			reason == EntitySpawnReason.TRIGGERED
			&& spawnItem != null
			&& spawnItem == StackUtil.getCallerAsProxy(0).getDeclaringClass()
		) {
			// Ironically, upstream's changes made it harder to filter.
			// Selectively filter TRIGGERED with the correct calling class.
			reason = EntitySpawnReason.SPAWN_ITEM_USE;
		}
		ImmutableEntities.makeImmutable(player, self, reason);
	}

	@Override
	public void fireblanket$setImmutable(final boolean immutable) {
		// NoGravity means no movement
		this.setNoGravity(immutable);
		// ItemFrame special
		this.setFixed(immutable);
	}

	@Override
	public void fireblanket$setNoMovement(final boolean noMovement) {
		this.setNoGravity(noMovement);
	}

	@Shadow
	public void setFixed(boolean fixed) {
		throw new AssertionError();
	}
}
