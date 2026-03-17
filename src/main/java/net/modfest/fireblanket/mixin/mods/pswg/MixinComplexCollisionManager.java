package net.modfest.fireblanket.mixin.mods.pswg;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.Optional;

@Pseudo
@Mixin(targets = "com.parzivail.util.entity.collision.ComplexCollisionManager")
public class MixinComplexCollisionManager {

	/**
	 * @reason Causes huge performance issues due to getOtherEntities calls. We're not using any PSWG complex entities, so just delete it.
	 * @author Una
	 */
	@Overwrite
	public static Optional<Vec3> adjustMovementForCollisions(Entity entity, Vec3 currentMovement) {
		return Optional.empty();
	}

}
