package net.modfest.fireblanket.mixin.log;

import net.minecraft.block.spawner.TrialSpawnerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TrialSpawnerData.class)
public class MixinTrialSpawnerData {


	@Redirect(
		method = "getAdditionalPlayers",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;logErrorOrPause(Ljava/lang/String;)V")
	)
	private void fireblanket$noLog(String message) {

	}
}
