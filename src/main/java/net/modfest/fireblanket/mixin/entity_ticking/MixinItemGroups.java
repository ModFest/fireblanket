package net.modfest.fireblanket.mixin.entity_ticking;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeTabs.class)
public class MixinItemGroups {
	@Inject(method = "method_51311(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab$Output;accept(Lnet/minecraft/world/level/ItemLike;)V", ordinal = 8))
	private static void fireblanket$addHammers(CreativeModeTab.ItemDisplayParameters ctx, CreativeModeTab.Output entries, CallbackInfo ci) {
		ItemStack noai = new ItemStack(Items.DEBUG_STICK);
		noai.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, n -> {
			CompoundTag nbt = new CompoundTag();
			nbt.putBoolean("NoAI", true);
			return CustomData.of(nbt);
		});
		entries.accept(noai);

		ItemStack nograv = new ItemStack(Items.DEBUG_STICK);
		nograv.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, n -> {
			CompoundTag nbt = new CompoundTag();
			nbt.putBoolean("NoGravity", true);
			return CustomData.of(nbt);
		});
		entries.accept(nograv);

		ItemStack nomov = new ItemStack(Items.DEBUG_STICK);
		nomov.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, n -> {
			CompoundTag nbt = new CompoundTag();
			nbt.putBoolean("NoMovement", true);
			return CustomData.of(nbt);
		});
		entries.accept(nomov);

		ItemStack noVehicle = new ItemStack(Items.DEBUG_STICK);
		noVehicle.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, n -> {
			CompoundTag nbt = new CompoundTag();
			nbt.putBoolean("NoVehicleEntering", true);
			return CustomData.of(nbt);
		});
		entries.accept(noVehicle);
	}
}
