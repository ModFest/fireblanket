package net.modfest.fireblanket.world;

import net.modfest.fireblanket.world.blocks.UpdateSignBlockEntityTypes;
import net.modfest.fireblanket.world.entity.EntityFilters;

public class WorldLoadAppliers {
	private static boolean ran = false;

	public static void init() {
		if (ran) {
			return;
		}
		ran = true;

		EntityFilters.apply();
	}
}
