package net.modfest.fireblanket.mixin.adventure_fix;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EggItem.class)
public class MixinEggItem {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void fireblanket$spamPrevention(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		if (!user.getAbilities().allowModifyWorld) {
			cir.setReturnValue(TypedActionResult.pass(user.getStackInHand(hand)));
		}
	}
}
