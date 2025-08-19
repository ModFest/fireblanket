package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemGroups.class)
public class MixinItemGroups {
	@Inject(method = "method_51311(Lnet/minecraft/item/ItemGroup$DisplayContext;Lnet/minecraft/item/ItemGroup$Entries;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup$Entries;add(Lnet/minecraft/item/ItemConvertible;)V", ordinal = 8))
	private static void fireblanket$addHammers(ItemGroup.DisplayContext ctx, ItemGroup.Entries entries, CallbackInfo ci) {
		ItemStack noai = new ItemStack(Items.DEBUG_STICK);
		noai.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, n -> {
			NbtCompound nbt = new NbtCompound();
			nbt.putBoolean("NoAI", true);
			return NbtComponent.of(nbt);
		});
		entries.add(noai);

		ItemStack nograv = new ItemStack(Items.DEBUG_STICK);
		nograv.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, n -> {
			NbtCompound nbt = new NbtCompound();
			nbt.putBoolean("NoGravity", true);
			return NbtComponent.of(nbt);
		});
		entries.add(nograv);

		ItemStack nomov = new ItemStack(Items.DEBUG_STICK);
		nomov.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, n -> {
			NbtCompound nbt = new NbtCompound();
			nbt.putBoolean("NoMovement", true);
			return NbtComponent.of(nbt);
		});
		entries.add(nomov);

		ItemStack noVehicle = new ItemStack(Items.DEBUG_STICK);
		noVehicle.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, n -> {
			NbtCompound nbt = new NbtCompound();
			nbt.putBoolean("NoVehicleEntering", true);
			return NbtComponent.of(nbt);
		});
		entries.add(noVehicle);
	}
}
