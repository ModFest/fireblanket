package net.modfest.fireblanket.mixin.footgun;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.modfest.fireblanket.mixinsupport.ForceableArgument;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class MixinEntitySelectorOptions {
	@Shadow private static void putOption(String id, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> condition, Text description) {
		throw new IllegalStateException("Unimplemented mixin");
	}

	@SuppressWarnings("rawtypes")
	@Shadow @Final private static Map OPTIONS;

	@Inject(method = "register", at = @At("TAIL"))
	private static void injectForce(CallbackInfo info) {
		if (!OPTIONS.containsKey("force")) {
			putOption("force", reader -> {
				reader.setLocalWorldOnly();
				((ForceableArgument) reader).setForced(reader.getReader().readBoolean());
			}, reader -> !((ForceableArgument) reader).isForced(), Text.translatable("argument.entity.options.force.description"));
		}
	}
}
