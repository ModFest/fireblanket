package net.modfest.fireblanket.mixin.mods.patchouli;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "vazkii.patchouli.common.handler.LecternEventHandler")
public class MixinLecternEventHandler {

	@ModifyExpressionValue(method = "rightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;shouldCancelInteraction()Z"))
	private static boolean stopTakingMyBooks(boolean original, @Local(argsOnly = true)PlayerEntity player) {
		return original && player.getAbilities().allowModifyWorld;
	}
}
