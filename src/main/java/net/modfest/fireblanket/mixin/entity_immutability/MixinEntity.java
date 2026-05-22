package net.modfest.fireblanket.mixin.entity_immutability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.modfest.fireblanket.mixinsupport.EntityWithReason;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * @author Ampflower
 **/
@Mixin(Entity.class)
public class MixinEntity implements EntityWithReason {
	@Unique
	private @Nullable EntitySpawnReason reason;


	@Override
	public void fireblanket$setReason(final EntitySpawnReason reason) {
		this.reason = reason;
	}

	@Override
	public EntitySpawnReason fireblanket$getReason() {
		return this.reason;
	}
}
