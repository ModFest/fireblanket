package net.modfest.fireblanket.mixin.opto;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Ampflower
 */
@Mixin(Direction.class)
public class MixinDirection {
	/**
	 * @vanilla-copy {@link Direction#getRotation()}
	 */
	@Unique
	private static final Quaternionf
		DOWN_ROTATION = new Quaternionf().rotationX(Mth.PI),
		UP_ROTATION = new Quaternionf(),
		NORTH_ROTATION = new Quaternionf().rotationXYZ(Mth.HALF_PI, 0.0F, Mth.PI),
		SOUTH_ROTATION = new Quaternionf().rotationX(Mth.HALF_PI),
		WEST_ROTATION = new Quaternionf().rotationXYZ(Mth.HALF_PI, 0.0F, Mth.HALF_PI),
		EAST_ROTATION = new Quaternionf().rotationXYZ(Mth.HALF_PI, 0.0F, -Mth.HALF_PI);

	/**
	 * @author Ampflower
	 * @reason Micro-optimisation of simply only copy the quaternions instead of rotating.
	 */
	@Overwrite
	public Quaternionf getRotation() {
		return new Quaternionf(switch ((Direction) (Object) this) {
			case DOWN -> DOWN_ROTATION;
			case UP -> UP_ROTATION;
			case NORTH -> NORTH_ROTATION;
			case SOUTH -> SOUTH_ROTATION;
			case WEST -> WEST_ROTATION;
			case EAST -> EAST_ROTATION;
		});
	}
}
