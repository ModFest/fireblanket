package net.modfest.fireblanket.mixin.diagnostics.net;

import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.network.Connection;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Ampflower
 */
@Mixin(Connection.class)
public class MixinConnection {
	@Redirect(method = "exceptionCaught", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;debug(Ljava/lang/String;Ljava/lang/Throwable;)V"))
	private void butMojangPeopleNeedToBeAbleToDebugTheirPackets(final Logger logger, final String message, final Throwable throwable) {
		logger.warn(message, throwable);

		if (throwable instanceof ReportedException reportedException) {
			logger.warn("Fireblanket: Crash report dump:\n{}", reportedException.getReport().getFriendlyReport(ReportType.NETWORK_PROTOCOL_ERROR));
		}
	}
}
