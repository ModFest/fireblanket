package net.modfest.fireblanket.mixin.mods.hotdognalds;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Ampflower
 */
@Pseudo
@Mixin(targets = "dev.chililisoup.hotdognalds.entity.CondimentDispenser")
public abstract class MixinCondimentDispenser extends Entity implements ImmutableEntities {

	private MixinCondimentDispenser(final EntityType<?> type, final Level level) {
		super(type, level);
	}

	@Inject(method = "create", at = @At("RETURN"))
	private static void fireblanket$onCreate(
		final CallbackInfoReturnable<@Nullable MixinCondimentDispenser> cir,
		final @Local(argsOnly = true) EntitySpawnReason reason,
		final @Local(argsOnly = true) @Nullable Player player
	) {
		final MixinCondimentDispenser self = cir.getReturnValue();
		if (self == null) {
			return;
		}

		ImmutableEntities.makeImmutable(player, self, reason);
	}

	@Override
	public void fireblanket$setImmutable(final boolean immutable) {
		// NoGravity means no movement.
		this.setNoGravity(immutable);
	}
}
