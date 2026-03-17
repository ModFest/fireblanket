package net.modfest.fireblanket.mixin.client.color;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class MixinItemStack { //implements ColorStack {
//	private Box<ItemColorProvider> fireblanket$colorProvider;
//	private Int2IntOpenHashMap fireblanket$tintsMap = new Int2IntOpenHashMap();
//
//	@Override
//	public Box<ItemColorProvider> fireblanket$getProvider() {
//		return fireblanket$colorProvider;
//	}
//
//	@Override
//	public void fireblanket$setProvider(ItemColorProvider provider) {
//		fireblanket$colorProvider = new Box<>(provider);
//	}
//
//	@Override
//	public Int2IntOpenHashMap fireblanket$getTintsMap() {
//		return fireblanket$tintsMap;
//	}
}
