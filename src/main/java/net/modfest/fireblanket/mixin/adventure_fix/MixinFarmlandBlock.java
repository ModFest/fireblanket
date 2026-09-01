package net.modfest.fireblanket.mixin.adventure_fix;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public class MixinFarmlandBlock {
	@Inject(
		method = "turnToBaseBlock",
		at = @At("HEAD"),
		cancellable = true
	)
	private void doNotSetToDirt(CallbackInfo ci, @Local(argsOnly = true) Entity entity) {
		if (entity instanceof Player player && !player.mayBuild()) {
			ci.cancel();
		}
	}
}
