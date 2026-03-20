package net.modfest.fireblanket.mixin.diagnostics.commands;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.Commands;
import net.modfest.fireblanket.Fireblanket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(Commands.class)
public class MixinCommands {
	@Inject(
		method = "performCommand",
		at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z")
	)
	private static void whatWeAreAlwaysInADeveloperEnvironmentWhatDoYouMean(
		final CallbackInfo ci,
		final @Local(argsOnly = true) String commandString,
		final @Local Exception exception
	) {
		Fireblanket.LOGGER.warn("Command `/{}` threw an exception", commandString, exception);
	}
}
