package net.modfest.fireblanket.mixinsupport;

import net.minecraft.world.entity.EntitySpawnReason;
import org.jspecify.annotations.NullUnmarked;

/**
 * @author Ampflower
 **/
@NullUnmarked
public interface EntityWithReason {
	void fireblanket$setReason(final EntitySpawnReason reason);

	EntitySpawnReason fireblanket$getReason();
}
