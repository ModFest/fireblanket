package net.modfest.fireblanket.mixin.mods.patchouli;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "vazkii.patchouli.common.handler.LecternEventHandler")
public class MixinLecternEventHandler {

	@ModifyExpressionValue(method = "rightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSecondaryUseActive()Z"))
	private static boolean stopTakingMyBooks(boolean original, @Local(argsOnly = true) Player player) {
		return original && player.getAbilities().mayBuild;
	}
}
