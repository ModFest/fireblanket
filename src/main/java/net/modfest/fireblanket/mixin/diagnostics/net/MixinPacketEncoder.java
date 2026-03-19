package net.modfest.fireblanket.mixin.diagnostics.net;

import com.llamalad7.mixinextras.sugar.Local;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.modfest.fireblanket.diagnostics.Dump;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dumps packets written to the logger.
 *
 * @author Ampflower
 **/
@Mixin(PacketEncoder.class)
public class MixinPacketEncoder {
	@Shadow
	@Final
	private ProtocolInfo<?> protocolInfo;

	@Inject(method = "encode", at = @At(value = "INVOKE", target = "Lio/netty/buffer/ByteBuf;readableBytes()I"))
	private void onEncode(
		final CallbackInfo ci,
		final @Local(argsOnly = true) Packet<?> packet,
		final @Local(argsOnly = true) ByteBuf buf
	) {
		// ForbiddenStackWalker.print(packet);
		Dump.buf(buf, "WRITE %s @ %s", packet, this.protocolInfo);
	}
}
