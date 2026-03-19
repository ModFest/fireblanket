package net.modfest.fireblanket.mixin.diagnostics;

import net.minecraft.CrashReport;
import net.modfest.fireblanket.diagnostics.ForbiddenStackWalker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Occasionally, a crash report can be emitted that is otherwise swallowed.
 * <p>
 * I don't think you'll be missing it when this prints, and you still get the context!
 * Probably, more context than you originally wanted, but still, context.
 *
 * @author Ampflower
 **/
@Mixin(CrashReport.class)
public class MixinCrashReport {
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void onConstruction(CallbackInfo ci) {
		ForbiddenStackWalker.dumpStack();
	}
}
