package net.modfest.fireblanket.mixin.opto;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MapState.class)
public class MixinMapState {
	/**
	 * @author jaskarth
	 *
	 * @reason don't
	 */
	@Overwrite
	public void update(PlayerEntity player, ItemStack stack) {

	}
}
