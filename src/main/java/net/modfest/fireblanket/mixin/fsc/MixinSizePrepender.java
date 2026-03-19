package net.modfest.fireblanket.mixin.fsc;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Varint21LengthFieldPrepender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Varint21LengthFieldPrepender.class)
public class MixinSizePrepender {

	@ModifyConstant(constant = @Constant(intValue = 3), method = "encode")
	public int fireblanket$liftPacketSizeLimit(int orig) {
		return 5;
	}

	@Definition(id = "readableBytes", method = "Lio/netty/buffer/ByteBuf;readableBytes()I")
	@Expression("? = ?.readableBytes()")
	@Inject(method = "encode", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), cancellable = true)
	private void zstdForceFlushPassthrough(
		final ChannelHandlerContext ctx,
		final ByteBuf msg,
		final ByteBuf out,
		final CallbackInfo ci,
		final @Local int readableBytes
	) {
		if (readableBytes == 0) {
			ci.cancel();
		}
	}

}
