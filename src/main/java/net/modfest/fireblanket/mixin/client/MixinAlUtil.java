package net.modfest.fireblanket.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AlUtil;
import net.minecraft.client.sound.SoundManager;
import net.modfest.fireblanket.Fireblanket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AlUtil.class)
public class MixinAlUtil {
	@Inject(method = "checkErrors", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/openal/AL10;alGetError()I"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private static void fireblanket$restartSoundEngineOnError(String sectionName, CallbackInfoReturnable<Boolean> cir, int error) {
		if (error != 0) {
			Fireblanket.LOGGER.warn("Restarting sound engine due to error!");
			MinecraftClient.getInstance().getSoundManager().soundSystem.stop();
			MinecraftClient.getInstance().getSoundManager().soundSystem.start();
			cir.setReturnValue(false);
		}
	}
}
