package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroups.class)
public class MixinItemGroups {
	@Inject(method = "method_51311(Lnet/minecraft/item/ItemGroup$DisplayContext;Lnet/minecraft/item/ItemGroup$Entries;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup$Entries;add(Lnet/minecraft/item/ItemConvertible;)V", ordinal = 8))
	private static void fireblanket$addHammers(ItemGroup.DisplayContext ctx, ItemGroup.Entries entries, CallbackInfo ci) {
		ItemStack noai = new ItemStack(Items.DEBUG_STICK);
		noai.getOrCreateNbt().putBoolean("NoAI", true);
		entries.add(noai);

		ItemStack nograv = new ItemStack(Items.DEBUG_STICK);
		nograv.getOrCreateNbt().putBoolean("NoGravity", true);
		entries.add(nograv);

		ItemStack nomov = new ItemStack(Items.DEBUG_STICK);
		nomov.getOrCreateNbt().putBoolean("NoMovement", true);
		entries.add(nomov);
	}
}
