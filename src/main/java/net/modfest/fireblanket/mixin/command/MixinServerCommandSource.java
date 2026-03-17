package net.modfest.fireblanket.mixin.command;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.util.TextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Ampflower
 **/
@Mixin(CommandSourceStack.class)
public class MixinServerCommandSource {
	/**
	 * @reason Replaces the display name with a full blame of the runner.
	 */
	@Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
	private void fireblanket$getRunnerName(CallbackInfoReturnable<Component> cir) {
		if (FireblanketConfig.get(ConfigSpecs.TATTLETALE_COMMANDS)) {
			cir.setReturnValue(TextUtil.ofRunner((CommandSourceStack) (Object) this));
		}
	}
}
