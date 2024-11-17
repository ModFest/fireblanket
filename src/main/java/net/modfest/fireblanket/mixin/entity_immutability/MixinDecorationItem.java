package net.modfest.fireblanket.mixin.entity_immutability;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.util.ImmutableEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecorationItem.class)
public class MixinDecorationItem {
	@Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getOrDefault(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
	private void onInitSpawnedEntity(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir, @Local AbstractDecorationEntity entity) {
		if (context.getWorld().getGameRules().getBoolean(Fireblanket.NEW_ENTITIES_IMMUTABLE)) {
			ImmutableEntities.makeImmutable(entity);
		}
	}
}
