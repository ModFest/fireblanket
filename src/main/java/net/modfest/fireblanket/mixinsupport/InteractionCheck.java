package net.modfest.fireblanket.mixinsupport;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.modfest.fireblanket.FireblanketConstants;

public class InteractionCheck {
	public static boolean preventUseItem(PlayerEntity player, ItemStack stack) {
		return stack.isIn(FireblanketConstants.ITEM_INTERACTION_RESTRICTED)
			||  (player.isSneaking() && stack.isIn(FireblanketConstants.ITEM_SNEAK_INTERACTION_RESTRICTED));
	}

	public static boolean preventUseBlock(PlayerEntity player, BlockState blockState) {
		return blockState.isIn(FireblanketConstants.BLOCK_INTERACTION_RESTRICTED)
			|| (player.isSneaking() && blockState.isIn(FireblanketConstants.BLOCK_SNEAK_INTERACTION_RESTRICTED));
	}

	public static boolean preventUseEntity(PlayerEntity player, EntityType<?> entity) {
		return entity.isIn(FireblanketConstants.ENTITY_INTERACTION_RESTRICTED)
			|| (player.isSneaking() && entity.isIn(FireblanketConstants.ENTITY_SNEAK_INTERACTION_RESTRICTED));
	}

	public static boolean preventAttackEntity(PlayerEntity player, EntityType<?> entity) {
		return entity.isIn(FireblanketConstants.ENTITY_ATTACK_RESTRICTED);
	}
}
