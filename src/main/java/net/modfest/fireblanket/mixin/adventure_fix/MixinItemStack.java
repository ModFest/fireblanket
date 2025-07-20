package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
//import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.modfest.fireblanket.FireblanketConstants;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
	@Shadow public abstract boolean isIn(TagKey<Item> tag);

	// corner case: item is allowed, but block interact is not allowed.
	// preventing use on blocks here might prevent non-interacting items
	// like rulers from simply using the blockpos.
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void fireblanket$filterItemUseOnBlockByTag(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
		PlayerEntity player = context.getPlayer();
		if (player == null) return;
		if (!player.getAbilities().allowModifyWorld && InteractionCheck.preventUseItem(player, (ItemStack)(Object)this)) {
			ci.setReturnValue(ActionResult.FAIL);
			ci.cancel();
		}
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void fireblanket$filterItemUseByTag(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> ci) {
		if (!user.getAbilities().allowModifyWorld && InteractionCheck.preventUseItem(user, (ItemStack)(Object)this)) {
			ci.setReturnValue(ActionResult.FAIL);
			ci.cancel();
		}
	}
}
