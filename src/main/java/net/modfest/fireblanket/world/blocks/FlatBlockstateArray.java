package net.modfest.fireblanket.world.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;

public class FlatBlockstateArray {
	public static BlockState[] FROM_ID;

	public static void apply() {
		int size = Block.BLOCK_STATE_REGISTRY.size();

		if (size > 1048575) {
			if (FireblanketConfig.get(ConfigSpecs.FLATTEN_CHUNK_PALETTES)) {
				throw new IllegalStateException("Fireblanket cannot start! We're attempting to load " + size + " unique blockstates" +
					", but we can only support up to 1048575! Please disable the flatten-chunk-palettes option to continue.");
			} else {
				Fireblanket.LOGGER.warn("FIREBLANKET WARNING: Only 1048575 blockstates are supported, but we have " + size + "!");
			}
		}

		FROM_ID = new BlockState[size];
		int i = 0;
		for (BlockState b : Block.BLOCK_STATE_REGISTRY) {
			FROM_ID[i++] = b;
		}
	}
}
