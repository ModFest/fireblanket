package net.modfest.fireblanket.mixinsupport;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.modfest.fireblanket.FireblanketConstants;

public final class InteractionCheck {
	public static boolean preventUseItem(Player player, ItemStack stack) {
		return stack.is(FireblanketConstants.ITEM_INTERACTION_RESTRICTED)
			|| (player.isShiftKeyDown() && stack.is(FireblanketConstants.ITEM_SNEAK_INTERACTION_RESTRICTED));
	}

	public static boolean preventUseBlock(Player player, BlockState blockState) {
		return blockState.is(FireblanketConstants.BLOCK_INTERACTION_RESTRICTED)
			|| (player.isShiftKeyDown() && blockState.is(FireblanketConstants.BLOCK_SNEAK_INTERACTION_RESTRICTED));
	}

	public static boolean preventUseEntity(Player player, Entity entity) {
		return entity.is(FireblanketConstants.ENTITY_INTERACTION_RESTRICTED)
			|| (player.isShiftKeyDown() && entity.is(FireblanketConstants.ENTITY_SNEAK_INTERACTION_RESTRICTED));
	}

	public static boolean preventAttackEntity(Player player, Entity entity) {
		return entity.is(FireblanketConstants.ENTITY_ATTACK_RESTRICTED);
	}
}
