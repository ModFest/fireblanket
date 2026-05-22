package net.modfest.fireblanket.mixin.datafixerupper;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.diagnostics.ForbiddenStackWalker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Function;

/**
 * When encountering null, forcefully dumps all live frames into the logger, then throws an NPE.
 * <p>
 * This does not check {@link ConfigSpecs#STRICT_CHECKS} as passing null into the DFU will
 * almost always cause hard to diagnose bugs, and it's <em>very unlikely</em> to be actually
 * intended and correctly accounted for.
 * <p>
 * Due to how otherwise useless NPEs are when it intersects the DataFixerUpper,
 * this shall remain always enabled as the {@link ForbiddenStackWalker} provides infinitely more
 * relevant context.
 * <p>
 * This could theoretically be changed to be lazy instead and only print the stack upon an NPE,
 * but this is more likely to cause highly verbose, repeated logs.
 *
 * @author Ampflower
 */
@Mixin(RecordCodecBuilder.class)
public class MixinRecordCodecBuilder<O, F> {

	@Shadow
	@Final
	private MapDecoder<F> decoder;

	{
		Objects.requireNonNull(decoder, "decoder");
	}

	@Definition(
		id = "getter",
		field = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;getter:Ljava/util/function/Function;"
	)
	@Expression("this.getter = @(?)")
	@ModifyExpressionValue(
		method = "<init>",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private <A, B> Function<A, B> nullCheckGetter(final Function<A, B> getter) {
		Objects.requireNonNull(getter, "getter");
		return a -> {
			if (a == null) {
				ForbiddenStackWalker.dumpStackAndThrow(NullPointerException::new);
			}
			return getter.apply(a);
		};
	}

	@Definition(
		id = "encoder",
		field = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;encoder:Ljava/util/function/Function;"
	)
	@Expression("this.encoder = @(?)")
	@ModifyExpressionValue(
		method = "<init>",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private <A, B> Function<A, B> nullCheckEncoder(final Function<A, B> encoder) {
		Objects.requireNonNull(encoder, "encoder");
		return a -> {
			if (a == null) {
				ForbiddenStackWalker.dumpStackAndThrow(NullPointerException::new);
			}
			return encoder.apply(a);
		};
	}

	@Mixin(targets = "com/mojang/serialization/codecs/RecordCodecBuilder$2")
	public static class Anonymous {
		/**
		 * @reason This is a common intersection point and this spot provides
		 * 	the most amount of direct context required to diagnose any given issue.
		 */
		@Inject(method = "encode", at = @At("HEAD"), require = 0)
		private void inspect(CallbackInfoReturnable<?> cir, @Local(argsOnly = true) Object object) {
			if (object == null) {
				ForbiddenStackWalker.dumpStackAndThrow(NullPointerException::new, this);
			}
		}
	}

	@Mixin(RecordCodecBuilder.Instance.class)
	public static class Instance {
		/**
		 * @reason Common crash point
		 */
		@Redirect(
			method = "lambda$lift1$1",
			at = @At(
				value = "INVOKE",
				target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"
			)
		)
		private <A, B> B inspect(final Function<A, B> self, final A a) {
			if (self == null || a == null) {
				ForbiddenStackWalker.dumpStackAndThrow(NullPointerException::new, this, self, a);
			}
			return self.apply(a);
		}
	}
}
