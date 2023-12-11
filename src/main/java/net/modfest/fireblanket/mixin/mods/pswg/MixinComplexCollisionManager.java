package net.modfest.fireblanket.mixin.mods.pswg;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Pseudo
@Mixin(targets="com.parzivail.util.entity.collision.ComplexCollisionManager")
public class MixinComplexCollisionManager {

	/**
	 * @reason Causes huge performance issues due to getOtherEntities calls. We're not using any PSWG complex entities, so just delete it.
	 * @author Una
	 */
	@Overwrite
	public static Optional<Vec3d> adjustMovementForCollisions(Entity entity, Vec3d currentMovement) {
		return Optional.empty();
	}
	
}
