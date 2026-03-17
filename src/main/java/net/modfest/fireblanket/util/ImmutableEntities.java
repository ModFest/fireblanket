package net.modfest.fireblanket.util;

import net.minecraft.world.entity.Entity;
import net.modfest.fireblanket.mixin.accessor.ArmorStandEntityAccessor;
import net.modfest.fireblanket.mixin.accessor.ItemFrameAccessor;
import net.modfest.fireblanket.mixinsupport.ImmmovableLivingEntity;

public class ImmutableEntities {
	public static void makeImmutable(Entity entity) {
		// Set invulnerability
		entity.setInvulnerable(true);

		if (entity instanceof ArmorStandEntityAccessor ae) {
			// Disable all slots
			ae.setDisabledSlots(4144959);
			// Disable movement (prevents abuse of fishing rods)
			if (entity instanceof ImmmovableLivingEntity im) {
				im.setNoMovement(true);
			}
		}

		if (entity instanceof ItemFrameAccessor ie) {
			// Make item frames fixed
			ie.setFixed(true);
		}
	}
}
