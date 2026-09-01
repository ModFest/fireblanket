package net.modfest.fireblanket.world.blocks;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.mixin.accessor.BlockEntityTypeAccessor;

import java.util.HashSet;

public class UpdateSignBlockEntityTypes {
	public static void apply(Block block) {
		try {
			if (block instanceof WallHangingSignBlock || block instanceof CeilingHangingSignBlock) {
				BlockEntityTypeAccessor sign = (BlockEntityTypeAccessor) BlockEntityTypes.HANGING_SIGN;

				if (!(sign.getValidBlocks() instanceof HashSet<Block>)) {
					sign.setValidBlocks(new HashSet<>(sign.getValidBlocks()));
				}

				sign.getValidBlocks().add(block);
				Fireblanket.LOGGER.debug(
					"Force-registered a hanging sign block entity: {}",
					BuiltInRegistries.BLOCK.getKey(block)
				);
			}

			if (block instanceof StandingSignBlock || block instanceof WallSignBlock) {
				BlockEntityTypeAccessor sign = (BlockEntityTypeAccessor) BlockEntityTypes.SIGN;

				if (!(sign.getValidBlocks() instanceof HashSet<Block>)) {
					sign.setValidBlocks(new HashSet<>(sign.getValidBlocks()));
				}

				sign.getValidBlocks().add(block);
				Fireblanket.LOGGER.debug(
					"Force-registered a sign block entity: {}",
					BuiltInRegistries.BLOCK.getKey(block)
				);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
