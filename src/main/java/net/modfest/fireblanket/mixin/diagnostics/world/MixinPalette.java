package net.modfest.fireblanket.mixin.diagnostics.world;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.LinearPalette;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.SingleValuePalette;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Found to have been passed null, causing major problems.
 *
 * @author Ampflower
 */
@Mixin(
	{
		LinearPalette.class,
		HashMapPalette.class,
		SingleValuePalette.class,
		GlobalPalette.class,
	}
)
public abstract class MixinPalette<T> implements Palette<T> {
	@Inject(method = "idFor", at = @At("HEAD"), cancellable = true)
	private void nullCheckIdFor(final CallbackInfoReturnable<Integer> cir, final @Local(argsOnly = true) Object first) {
		if (first != null) {
			return;
		}
		if (FireblanketConfig.get(ConfigSpecs.STRICT_CHECKS)) {
			throw new NullPointerException("These data structures are not designed to handle null. Don't do that.");
		}
		if (this.getSize() == 0) {
			throw new NullPointerException(
				this + " is empty, throwing anyways as adding null and returning 0 are unsafe.\n" +
				"These data structures are not designed to handle null. Don't do that."
			);
		}

		Fireblanket.LOGGER.warn(
			"Null passed to {}, returning {}. These data structures are not designed to handle null. Don't do that.",
			this,
			this.valueFor(0),
			new Throwable()
		);

		cir.setReturnValue(0);
	}
}
