package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Some mods apparently will spawn fire charges. Reduce the griefing potential.
 * <p>
 * As an amusing aside, it makes Fireblanket live up to its name.
 *
 * @author Ampflower
 **/
@Mixin(SmallFireballEntity.class)
public abstract class MixinSmallFireballEntity extends AbstractFireballEntity {

	public MixinSmallFireballEntity(final EntityType<? extends AbstractFireballEntity> entityType, final World world) {
		super(entityType, world);
	}

	@ModifyExpressionValue(
		method = "onBlockHit",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;isAir(Lnet/minecraft/util/math/BlockPos;)Z"
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
		final ServerWorld world = (ServerWorld) this.getWorld();
		// Annoyingly, we can't just @Local it.
		final Entity entity = this.getOwner();

		if (entity == null && !world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
			return false;
		}

		return !(entity instanceof ServerPlayerEntity player) || player.canModifyAt(world, blockPos);
	}
}
