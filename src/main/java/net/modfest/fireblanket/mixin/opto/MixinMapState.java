package net.modfest.fireblanket.mixin.opto;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(MapState.class)
public class MixinMapState {
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;contains(Ljava/util/function/Predicate;)Z"))
	private boolean fireblanket$contains(PlayerInventory instance, Predicate<ItemStack> predicate) {
		return false;
	}
}
