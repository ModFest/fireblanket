package net.modfest.fireblanket.mixin.crash;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CraftingMenu.class)
public class MixinCraftingScreenHandler {
	/**
	 * @author jaskarth
	 *
	 * @reason it crashes
	 */
	@Overwrite
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}
}
