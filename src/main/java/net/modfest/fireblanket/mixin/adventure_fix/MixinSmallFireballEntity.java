package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Some mods apparently will spawn fire charges. Reduce the griefing potential.
 * <p>
 * As an amusing aside, it makes Fireblanket live up to its name.
 *
 * @author Ampflower
 **/
@Mixin(SmallFireball.class)
public abstract class MixinSmallFireballEntity extends Fireball {

	public MixinSmallFireballEntity(final EntityType<? extends Fireball> entityType, final Level world) {
		super(entityType, world);
	}

	@ModifyExpressionValue(
		method = "onHitBlock",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;isEmptyBlock(Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean fireblanket$madeAccurate(
		final boolean original,
		final @Local BlockPos blockPos
	) {
		if (!original) {
			return false;
		}

		// we're already past the gate, don't bother to check.
		final ServerLevel world = (ServerLevel) this.level();
		// Annoyingly, we can't just @Local it.
		final Entity entity = this.getOwner();

		if (entity == null && !world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
			return false;
		}

		return !(entity instanceof ServerPlayer player) || player.mayInteract(world, blockPos);
	}
}
