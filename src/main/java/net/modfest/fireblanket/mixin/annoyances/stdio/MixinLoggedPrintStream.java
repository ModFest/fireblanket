package net.modfest.fireblanket.mixin.annoyances.stdio;

import net.minecraft.server.LoggedPrintStream;
import net.modfest.fireblanket.stacksmash.StackUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Ampflower
 **/
@Mixin(LoggedPrintStream.class)
public class MixinLoggedPrintStream {
	@Redirect(method = "logLine", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
	private static void onLog(final Logger instance, final String format, final Object name, final Object value) {
		LoggerFactory.getLogger(StackUtil.getCallerAsProxy(1).getDeclaringClass()).info(format, name, value);
	}
}
