package net.modfest.fireblanket.mixin.annoyances;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Util.class)
public class MixinUtil {
	@WrapWithCondition(method = "doFetchChoiceType", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"))
	private static boolean dontWarnNoDataFixer(Logger instance, String s, Object o) {
		return false;
	}
}
