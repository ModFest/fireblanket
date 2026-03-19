package net.modfest.fireblanket.mixin.diagnostics.net;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.CompressionDecoder;
import net.modfest.fireblanket.diagnostics.Dump;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(CompressionDecoder.class)
public class MixinCompressionDecoder {
	/**
	 * Unconditionally dumps the construction of the inflater.
	 * <p>
	 * Admittedly, ForbiddenStackWalker is overkill here.
	 */
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void onConstruction(CallbackInfo ci) {
		// Commented out because it's probably more annoying than useful unless you're tracing a race condition.
		// ForbiddenStackWalker.dumpStack();
	}

	@Inject(method = "decode", at = @At("HEAD"))
	private void beforeInput(
		final CallbackInfo ci,
		final @Local(argsOnly = true) ByteBuf buf,
		final @Share("fireblanket:meow") LocalRef<ByteBuf> sliced
	) {
		sliced.set(buf.retainedSlice());
	}

	@WrapOperation(method = "decode", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/CompressionDecoder;inflate(Lio/netty/channel/ChannelHandlerContext;I)Lio/netty/buffer/ByteBuf;"))
	private ByteBuf wrapInflation(
		final CompressionDecoder self,
		final ChannelHandlerContext ctx,
		final int len,
		final Operation<ByteBuf> operation,
		final @Share("fireblanket:meow") LocalRef<ByteBuf> sliced
	) throws Exception {
		try {
			return operation.call(self, ctx, len);
		} catch (Exception e) {
			Dump.crashedBuf(sliced.get(), e, "INFLA @ ???");
			throw e;
		} finally {
			sliced.get().release();
		}
	}
}
