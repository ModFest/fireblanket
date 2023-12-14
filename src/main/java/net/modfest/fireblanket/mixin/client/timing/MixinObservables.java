package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {Entity.class, BlockEntity.class})
public class MixinObservables implements ObservableTicks {
	private long fireblanket$tickTime;

	@Override
	public void fireblanket$setTickTime(long val) {
		fireblanket$tickTime = val;
	}

	@Override
	public long fireblanket$getTickTime() {
		return fireblanket$tickTime;
	}
}
