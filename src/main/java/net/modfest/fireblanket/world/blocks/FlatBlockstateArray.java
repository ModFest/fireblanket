package net.modfest.fireblanket.world.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class FlatBlockstateArray {
	public static BlockState[] FROM_ID;
	public static void apply() {
		int size = Block.STATE_IDS.size();

		FROM_ID = new BlockState[size];
		int i = 0;
		for (BlockState b : Block.STATE_IDS) {
			FROM_ID[i++] = b;
		}
	}
}
