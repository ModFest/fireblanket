package net.modfest.fireblanket.mixin.stack;

import net.minecraft.core.Holder;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.modfest.fireblanket.mixinsupport.IdStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack implements IdStack {
	@Unique
	private int fireblanket$id = 0;

	@Inject(
		method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
		at = @At("TAIL")
	)
	private void fireblanket$initFull(Holder<Item> item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
		fireblanket$id = BuiltInRegistries.ITEM.getId(item.value());
	}

	@Override
	public int fireblanket$getRawId() {
		return fireblanket$id;
	}
}
