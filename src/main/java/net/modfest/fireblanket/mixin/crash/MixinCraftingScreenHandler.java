package net.modfest.fireblanket.mixin.crash;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(CraftingScreenHandler.class)
public class MixinCraftingScreenHandler {
	/**
	 * @author jaskarth
	 *
	 * @reason it crashes
	 */
	@Overwrite
	public ItemStack quickMove(PlayerEntity player, int slot) {
		return ItemStack.EMPTY;
	}
}
