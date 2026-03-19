package net.modfest.fireblanket.mixin.diagnostics.net;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.modfest.fireblanket.diagnostics.Dump;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * Dumps packets read to the logger, even on error.
 *
 * @author Ampflower
 **/
@Mixin(PacketDecoder.class)
@Debug(export = true)
public class MixinPacketDecoder {
	@Shadow
	@Final
	private ProtocolInfo<?> protocolInfo;

	@Inject(method = "decode", at = @At("HEAD"))
	private void storeContext(
		final CallbackInfo ci,
		final @Local(argsOnly = true) ByteBuf buf,
		final @Share("fireblanket:meow") LocalRef<ByteBuf> sliced
	) {
		sliced.set(buf.retainedSlice());
	}

	@Definition(id = "packet", local = @Local(type = Packet.class, name = "packet"))
	@Expression("packet = @(?)")
	@ModifyExpressionValue(
		method = "decode",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private Packet<?> onDecode(final Packet<?> original, @Share("fireblanket:meow") LocalRef<ByteBuf> sliced) {
		try {
			// ForbiddenStackWalker.print(original);
			Dump.buf(sliced.get(), "READ  %s @ %s", original, this.protocolInfo);
			return original;
		} finally {
			sliced.get().release();
		}
	}

	@Inject(method = "decode", at = @At(value = "INVOKE", target = "Lio/netty/buffer/ByteBuf;skipBytes(I)Lio/netty/buffer/ByteBuf;"))
	private void crash(
		final CallbackInfo ci,
		final @Local(argsOnly = true) ByteBuf buf,
		final @Local Exception e,
		final @Share("fireblanket:meow") LocalRef<ByteBuf> sliced
	) {
		try {
			Dump.buf(sliced.get(), "READ! @ %s", this.protocolInfo);
			Dump.crashedBuf(buf, e, "CRASH @ %s", this.protocolInfo);
		} finally {
			sliced.get().release();
		}
	}

	@Definition(id = "IOException", type = IOException.class)
	@Expression("throw @(new IOException(?))")
	@ModifyExpressionValue(method = "decode", at = @At("MIXINEXTRAS:EXPRESSION"))
	private IOException crash(final IOException e, final @Local(argsOnly = true) ByteBuf buf) {
		Dump.crashedBuf(buf, e, "CRASH @ %s", this.protocolInfo);
		return e;
	}
}
