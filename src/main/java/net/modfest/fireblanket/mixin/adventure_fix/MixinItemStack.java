package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.modfest.fireblanket.mixinsupport.InteractionCheck;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

	// corner case: item is allowed, but block interact is not allowed.
	// preventing use on blocks here might prevent non-interacting items
	// like rulers from simply using the blockpos.
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void fireblanket$filterItemUseOnBlockByTag(UseOnContext context, CallbackInfoReturnable<InteractionResult> ci) {
		Player player = context.getPlayer();
		if (player == null) return;
		if (!player.getAbilities().mayBuild && InteractionCheck.preventUseItem(player, (ItemStack) (Object) this)) {
			ci.setReturnValue(InteractionResult.FAIL);
			ci.cancel();
		}
	}

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void fireblanket$filterItemUseByTag(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {
		if (!user.getAbilities().mayBuild && InteractionCheck.preventUseItem(user, (ItemStack) (Object) this)) {
			ci.setReturnValue(InteractionResult.FAIL);
			ci.cancel();
		}
	}
}
