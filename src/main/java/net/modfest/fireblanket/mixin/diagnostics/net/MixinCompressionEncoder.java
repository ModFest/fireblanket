package net.modfest.fireblanket.mixin.diagnostics.net;

import net.minecraft.network.CompressionEncoder;
import net.modfest.fireblanket.diagnostics.ForbiddenStackWalker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Unconditionally dumps the creation of the deflater.
 * <p>
 * Admittedly, ForbiddenStackWalker is overkill here.
 *
 * @author Ampflower
 */
@Mixin(CompressionEncoder.class)
public class MixinCompressionEncoder {
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void onConstruction(CallbackInfo ci) {
		ForbiddenStackWalker.dumpStack();
	}
}
