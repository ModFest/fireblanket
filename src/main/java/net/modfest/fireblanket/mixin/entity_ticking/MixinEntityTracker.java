package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
public abstract class MixinEntityTracker {
	@Shadow
	@Final
	private int maxDistance;

	@Shadow
	protected abstract int adjustTrackingDistance(int initialDistance);

	/**
	 * @author jaskarth
	 *
	 * @reason Improved passenger logic
	 */
	@Overwrite
	private int getMaxTrackDistance() {
		int i = this.maxDistance;

		// No distance tracking - if needed, entity track distance can be modified.
//		for (Entity entity : this.entity.getPassengersDeep()) {
//			int j = entity.getType().getMaxTrackDistance() * 16;
//			if (j > i) {
//				i = j;
//			}
//		}

		return this.adjustTrackingDistance(i);
	}
}
