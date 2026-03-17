package net.modfest.fireblanket.mixin.entity_ticking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class MixinEntityTracker {
	@Shadow
	@Final
	private int range;

	@Shadow
	protected abstract int scaledRange(int initialDistance);

	/**
	 * @author jaskarth
	 *
	 * @reason Improved passenger logic
	 */
	@Overwrite
	private int getEffectiveRange() {
		int i = this.range;

		// No distance tracking - if needed, entity track distance can be modified.
//		for (Entity entity : this.entity.getPassengersDeep()) {
//			int j = entity.getType().getMaxTrackDistance() * 16;
//			if (j > i) {
//				i = j;
//			}
//		}

		return this.scaledRange(i);
	}
}
