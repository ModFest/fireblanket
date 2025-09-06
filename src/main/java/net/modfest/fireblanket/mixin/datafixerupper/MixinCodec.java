package net.modfest.fireblanket.mixin.datafixerupper;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * Adds validation layers to catch bugs that may otherwise cause unpredictable,
 * hard to diagnose issues later on.
 *
 * @author Ampflower
 */
@Mixin(value = Codec.class, remap = false)
public interface MixinCodec {

	/**
	 * This catches an otherwise hard to trace bug that requires stack inspection tooling at crash time,
	 * which is not feasible to use in production if it can be caught at all.
	 * <p>
	 * Should anyone ever have the misfortune of <em>needing</em> to diagnose a codec crash that only
	 * says NullPointerException, hook an in-depth stack dumper or debugger at the following locations:
	 * <ul>
	 *     <li>Constructor of {@link DataResult.Success}</li>
	 *     <li>{@link DataResult.Success#result()}</li>
	 *     <li>{@link DataResult.Success#resultOrPartial()}</li>
	 *     <li>{@link DataResult.Success#resultOrPartial(Consumer)} </li>
	 * </ul>
	 */
	@Inject(
		method = {
			"optionalFieldOf(Ljava/lang/String;Ljava/lang/Object;Z)Lcom/mojang/serialization/MapCodec;",
			"optionalFieldOf(Ljava/lang/String;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lcom/mojang/serialization/MapCodec;"
		}, at = @At("HEAD")
	)
	private void throwIfDefaultNull(
		CallbackInfoReturnable<?> cir,
		@Local(argsOnly = true) String name,
		@Local(argsOnly = true) Object defaultValue
	) {
		if (defaultValue == null) {
			throw new NullPointerException("null was given as default value for the optional field named " + name + ", expecting codec " + this.getClass()
				.getName() + "{" + this + "}");
		}
	}
}
