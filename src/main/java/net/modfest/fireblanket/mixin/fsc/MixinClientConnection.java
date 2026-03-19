package net.modfest.fireblanket.mixin.fsc;

import com.github.luben.zstd.ZstdOutputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.Fireblanket.QueuedPacket;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.net.ZstdDecoder;
import net.modfest.fireblanket.net.ZstdEncoder;
import net.modfest.fireblanket.util.LinkedBlocQueue;
import net.modfest.fireblanket.util.ReassignableOutputStream;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

@Mixin(Connection.class)
public abstract class MixinClientConnection implements FSCConnection {

	@Shadow
	private Channel channel;

	@Shadow
	private void sendPacket(Packet<?> packet, ChannelFutureListener callbacks, boolean flush) {
		throw new AbstractMethodError();
	}

	@Shadow
	private volatile @Nullable PacketListener packetListener;

	@Shadow
	public abstract void flushChannel();

	private final LinkedBlocQueue<QueuedPacket> fireblanket$queue = Fireblanket.getNextQueue();
	private boolean fireblanket$fsc = false;
	private boolean fireblanket$fscStarted = false;

	/**
	 * With a lot of connections, simply the act of writing packets becomes slow.
	 * Doing this on the server thread reduces TPS for no good reason.
	 *
	 * The client already does networking roughly like this, so the protocol stack is already
	 * designed to expect this behavior.
	 */
	@Redirect(
			method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;sendPacket(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"))
	public void fireblanket$asyncPacketSending(Connection subject, Packet<?> pkt, @Nullable ChannelFutureListener listener, boolean flush) {
//		System.out.println("Sending: " + pkt.getClass().getName() + " " + fireblanket$fsc + " " + fireblanket$fscStarted);
//		System.out.println("Sending: " + pkt.getClass().getName()
//			+ (pkt instanceof CustomPayloadC2SPacket(CustomPayload payload) ? " as " + payload.getId() : ""));

		PacketListener pktListener = this.packetListener;
		if (pktListener != null && pktListener.protocol() == ConnectionProtocol.PLAY && Fireblanket.IS_FIREBLANKET_SERVER) {
			fireblanket$queue.put(new QueuedPacket(subject, pkt, listener));
		} else {
			sendPacket(pkt, listener, flush);
		}
	}

	@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V", shift = At.Shift.BEFORE))
	public void fireblanket$receive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
//		System.out.println("Receive: " + packet.getClass().getName() + " " + fireblanket$fsc + " " + fireblanket$fscStarted);
//		System.out.println("Receive: " + packet.getClass().getName()
//			+ (packet instanceof CustomPayloadS2CPacket(CustomPayload payload) ? " as " + payload.getId() : ""));

		// idk man
		if (packet instanceof ClientboundDisconnectPacket || packet instanceof ClientboundLoginDisconnectPacket) {
			fireblanket$fscStarted = false;
			fireblanket$fsc = false;
		}
	}

	@Inject(at = @At("HEAD"), method = "setupCompression", cancellable = true)
	public void fireblanket$handleCompression(int threshold, boolean check, CallbackInfo ci) {
		if (fireblanket$fscStarted) {
			ci.cancel();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fireblanket$startFullStreamCompression(final long millis) {
		if (!this.fireblanket$fsc) {
			return;
		}
		if (!this.fireblanket$fscStarted) {
			this.fireblanket$enableFSCNow(millis);
		} else {
			this.fireblanket$modifyFSC(millis);
		}
	}

	private void fireblanket$enableFSCNow(final long millis) {
//		Thread.dumpStack();

		fireblanket$fscStarted = true;
		ChannelPipeline pipeline = channel.pipeline();
		Connection self = (Connection) (Object) this;
		try {
			boolean client = self.getReceiving() == PacketFlow.CLIENTBOUND;
			ReassignableOutputStream ros = new ReassignableOutputStream();
			ZstdOutputStream zos = new ZstdOutputStream(ros);
			zos.setLevel(client ? 6 : 4);
			zos.setLong(client ? 27 : 22);
			zos.setCloseFrameOnFlush(false);
			ZstdEncoder enc = new ZstdEncoder(ros, zos, millis);
			ZstdDecoder dec = new ZstdDecoder();
			if (pipeline.get("compress") != null) {
				pipeline.remove("compress");
			}
			if (pipeline.get("decompress") != null) {
				pipeline.remove("decompress");
			}
			pipeline.addBefore("prepender", "fireblanket:fsc_enc", enc);
			pipeline.addBefore("splitter", "fireblanket:fsc_dec", dec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	// io_uring makes it very difficult
	private void fireblanket$modifyFSC(final long millis) {
		final ChannelPipeline pipeline = channel.pipeline();

		if (!(pipeline.get("fireblanket:fsc_enc") instanceof ZstdEncoder encoder)) {
			throw new IllegalStateException("fireblanket:fsc_enc is missing or replaced despite starting fsc?");
		}

		encoder.setFlushFrequency(TimeUnit.MILLISECONDS.toNanos(millis));
	}

	@Override
	public void fireblanket$enableFullStreamCompression() {
		fireblanket$fsc = true;
	}
}
