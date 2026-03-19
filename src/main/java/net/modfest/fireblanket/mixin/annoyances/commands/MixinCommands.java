package net.modfest.fireblanket.mixin.annoyances.commands;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.commands.Commands;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * @author Ampflower
 **/
@Mixin(Commands.class)
public class MixinCommands {
	@ModifyExpressionValue(
		method = "performCommand",
		at = @At(value = "FIELD", target = "Lnet/minecraft/SharedConstants;DEBUG_VERBOSE_COMMAND_ERRORS:Z", opcode = Opcodes.GETSTATIC))
	private static boolean whatWeAreAlwaysInADeveloperEnvironmentWhatDoYouMean(boolean ignored) {
		return true;
	}
}
