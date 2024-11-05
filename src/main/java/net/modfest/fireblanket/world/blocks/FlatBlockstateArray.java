package net.modfest.fireblanket.world.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class FlatBlockstateArray {
	public static BlockState[] FROM_ID;

	public static void apply() {
		int size = Block.STATE_IDS.size();

		if (size > 1048575) {
			throw new IllegalStateException("Fireblanket cannot start! We're attempting to load " + size + " unique blockstates" +
					", but we can only support up to 1048575! Please disable the flatten-chunk-palettes option to continue.");
		}

		FROM_ID = new BlockState[size];
		int i = 0;
		for (BlockState b : Block.STATE_IDS) {
			FROM_ID[i++] = b;
		}
	}
}
