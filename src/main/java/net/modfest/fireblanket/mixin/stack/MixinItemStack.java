package net.modfest.fireblanket.mixin.stack;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.modfest.fireblanket.mixinsupport.IdStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack { //implements IdStack {
//	private int fireblanket$id = 0;
//
//	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/ComponentMapImpl;)V", at = @At("TAIL"))
//	private void fireblanket$initFull(ItemConvertible item, int count, ComponentMapImpl components, CallbackInfo ci) {
//		fireblanket$id = Registries.ITEM.getRawId(item.asItem());
//	}
//
//	@Override
//	public int fireblanket$getRawId() {
//		return fireblanket$id;
//	}
}
