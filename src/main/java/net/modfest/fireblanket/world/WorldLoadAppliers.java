package net.modfest.fireblanket.world;

import net.modfest.fireblanket.world.blocks.FlatBlockstateArray;
import net.modfest.fireblanket.config.EntityFilters;

public class WorldLoadAppliers {
	private static boolean ran = false;

	public static void init() {
		if (ran) {
			return;
		}
		ran = true;

		EntityFilters.apply();
		FlatBlockstateArray.apply();
	}
}
