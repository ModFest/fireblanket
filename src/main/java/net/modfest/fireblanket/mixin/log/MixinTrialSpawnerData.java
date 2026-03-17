package net.modfest.fireblanket.mixin.log;

import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TrialSpawnerStateData.class)
public class MixinTrialSpawnerData {


	@Redirect(
		method = "countAdditionalPlayers",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;logAndPauseIfInIde(Ljava/lang/String;)V")
	)
	private void fireblanket$noLog(String message) {

	}
}
