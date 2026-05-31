package net.modfest.fireblanket.mixin.diagnostics.streams;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.codec.StreamCodec;
import net.modfest.fireblanket.diagnostics.ForbiddenStackWalker;
import net.modfest.fireblanket.util.Throwables;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Coerce;

/**
 * @author Ampflower
 **/
@Mixin(StreamCodec.class)
public interface MixinStreamCodec {
	// unfortunately, this isn't very useful on its own.
	// Will leave this in if a more reliable reproduction is found.
/*
	@Inject(method = "cast", at = @At("HEAD"))
	private void cast(CallbackInfoReturnable<?> cir) {
		if (StackUtil.getCallerAsProxy(0).getDeclaringClass() == PayloadTypeRegistryImpl.class) {
			return;
		}
		if (this instanceof Throwables.AllocationTraceable traceable) {
			ForbiddenStackWalker.print("Possible cast mishap.", this, traceable.fireblanket$allocationTrace(), new Throwable());
		}
	}
*/
	@Mixin(
		targets = {
			"net.minecraft.network.codec.StreamCodec$1",
			"net.minecraft.network.codec.StreamCodec$2",
			"net.minecraft.network.codec.StreamCodec$3",
			"net.minecraft.network.codec.StreamCodec$4",
			"net.minecraft.network.codec.StreamCodec$5",
			"net.minecraft.network.codec.StreamCodec$6",
			"net.minecraft.network.codec.StreamCodec$7",
			"net.minecraft.network.codec.StreamCodec$8",
			"net.minecraft.network.codec.StreamCodec$9",
			"net.minecraft.network.codec.StreamCodec$10",
			"net.minecraft.network.codec.StreamCodec$11",
			"net.minecraft.network.codec.StreamCodec$12",
			"net.minecraft.network.codec.StreamCodec$13",
			"net.minecraft.network.codec.StreamCodec$14",
			"net.minecraft.network.codec.StreamCodec$15",
			"net.minecraft.network.codec.StreamCodec$16",
			"net.minecraft.network.codec.StreamCodec$17",
			"net.minecraft.network.codec.StreamCodec$18",
			"net.minecraft.network.codec.StreamCodec$19",
		}
	)
	class Anonymous<B, V> implements Throwables.AllocationTraceable {
		@Unique
		private final Throwable allocationTrace = new Throwable("Allocated").fillInStackTrace();

		@WrapMethod(method = "decode")
		private @Coerce V decode(@Coerce B input, Operation<V> operation) {
			try {
				return operation.call(input);
			} catch (Throwable t) {
				t.addSuppressed(allocationTrace);
				ForbiddenStackWalker.dumpStackAndThrow(() -> t, input);
				throw Throwables.assertSuppressed("FSW did not throw?", t, allocationTrace);
			}
		}

		@WrapMethod(method = "encode")
		private void encode(@Coerce B output, @Coerce V value, Operation<Void> operation) {
			try {
				operation.call(output, value);
			} catch (Throwable t) {
				t.addSuppressed(allocationTrace);
				ForbiddenStackWalker.dumpStackAndThrow(() -> t, output, value);
			}
		}

		@Override
		public Throwable fireblanket$allocationTrace() {
			return this.allocationTrace;
		}

		@Intrinsic
		@Override
		public String toString() {
			return super.toString();
		}

		@WrapMethod(method = "toString()Ljava/lang/String;")
		public String fireblanket$toString(Operation<String> operation) {
			final StringBuilder builder = new StringBuilder(operation.call()).append("{\n\t");
			ForbiddenStackWalker.print(builder, "\t", allocationTrace);
			return builder.append('}').toString();
		}
	}
}
