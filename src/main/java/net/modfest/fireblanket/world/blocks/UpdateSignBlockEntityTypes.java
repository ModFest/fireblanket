package net.modfest.fireblanket.world.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallHangingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.mixin.accessor.BlockEntityTypeAccessor;

import java.util.HashSet;

public class UpdateSignBlockEntityTypes {
	public static void apply(Block block) {
		try {
			if (block instanceof WallHangingSignBlock || block instanceof HangingSignBlock) {
				BlockEntityTypeAccessor sign = (BlockEntityTypeAccessor) BlockEntityType.HANGING_SIGN;

				if (!(sign.getBlocks() instanceof HashSet<Block>)) {
					sign.setBlocks(new HashSet<>(sign.getBlocks()));
				}

				sign.getBlocks().add(block);
				Fireblanket.LOGGER.debug("Force-registered a hanging sign block entity: " + Registries.BLOCK.getId(block));
			}

			if (block instanceof SignBlock || block instanceof WallSignBlock) {
				BlockEntityTypeAccessor sign = (BlockEntityTypeAccessor) BlockEntityType.SIGN;

				if (!(sign.getBlocks() instanceof HashSet<Block>)) {
					sign.setBlocks(new HashSet<>(sign.getBlocks()));
				}

				sign.getBlocks().add(block);
				Fireblanket.LOGGER.debug("Force-registered a sign block entity: " + Registries.BLOCK.getId(block));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
