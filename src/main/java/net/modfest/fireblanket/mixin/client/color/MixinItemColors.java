package net.modfest.fireblanket.mixin.client.color;

import net.minecraft.client.color.item.ItemTintSources;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemTintSources.class)
public class MixinItemColors {
//	@Shadow
//	@Final
//	private IdList<TintSource> providers;
//
//	private Int2ReferenceOpenHashMap<TintSource> fireblanket$map = new Int2ReferenceOpenHashMap<>();

	/**
	 * @author jaskarth
	 *
	 * @reason Use cached id for performance
	 *
	 * Needs major overhaul due to new tint handling
	 */
//	@Overwrite
//	public int getColor(ItemStack item, int tintIndex) {
//		int id = ((IdStack) (Object) item).fireblanket$getRawId();
//		ColorStack stack = (ColorStack) (Object) item;
//
//		Box<ItemColorProvider> box = stack.fireblanket$getProvider();
//		ItemColorProvider provider;
//		if (box != null) {
//			provider = box.value();
//		} else {
//			provider = this.fireblanket$map.get(id);
//			stack.fireblanket$setProvider(provider);
//		}
//		if (provider == null) {
//			return -1;
//		}
//
//		return provider.getColor(item, tintIndex);
//
//		// Slow?
////		Int2IntOpenHashMap map = stack.fireblanket$getTintsMap();
////		int v = map.get(tintIndex);
////		if (v == 0) {
////			v = provider.getColor(item, tintIndex);
////			map.put(tintIndex, v);
////		}
////		return v;
////		return provider == null ? -1 : provider.getColor(item, tintIndex);
//	}

//	@Inject(method = "register", at = @At("TAIL"))
//	private void fireblanket$addToMap(ItemColorProvider provider, ItemConvertible[] items, CallbackInfo ci) {
//		for (ItemConvertible item : items) {
//			fireblanket$map.put(Item.getRawId(item.asItem()), provider);
//		}
//	}
}
