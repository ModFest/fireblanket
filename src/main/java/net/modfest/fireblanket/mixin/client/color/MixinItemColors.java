package net.modfest.fireblanket.mixin.client.color;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.item.tint.TintSourceTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.IdList;
import net.modfest.fireblanket.mixinsupport.ColorStack;
import net.modfest.fireblanket.mixinsupport.IdStack;
import net.modfest.fireblanket.util.Box;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TintSourceTypes.class)
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
