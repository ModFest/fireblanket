package net.modfest.fireblanket.mixin.fsc;

import com.github.luben.zstd.ZstdOutputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.login.LoginDisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
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

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection implements FSCConnection {

	@Shadow
	private Channel channel;

	@Shadow
	private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush) {
		throw new AbstractMethodError();
	}

	@Shadow
	private volatile @Nullable PacketListener packetListener;

	@Shadow
	public abstract void flush();

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
	@Redirect(at=@At(value="INVOKE", target="net/minecraft/network/ClientConnection.sendImmediately(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V"),
			method="send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V")
	public void fireblanket$asyncPacketSending(ClientConnection subject, Packet<?> pkt, PacketCallbacks listener, boolean flush) {
//		System.out.println("Sending: " + pkt.getClass().getName() + " " + fireblanket$fsc + " " + fireblanket$fscStarted);
//		System.out.println("Sending: " + pkt.getClass().getName()
//			+ (pkt instanceof CustomPayloadC2SPacket(CustomPayload payload) ? " as " + payload.getId() : ""));

		// Server
		if (pkt instanceof GameJoinS2CPacket && fireblanket$fsc && !fireblanket$fscStarted) {
			fireblanket$enableFSCNow();
		}

		PacketListener pktListener = this.packetListener;
		if (pktListener != null && pktListener.getPhase() == NetworkPhase.PLAY && Fireblanket.IS_FIREBLANKET_SERVER) {
			fireblanket$queue.put(new QueuedPacket(subject, pkt, listener));
		} else {
			sendImmediately(pkt, listener, flush);
		}

		// Client
		if (pkt instanceof ReadyC2SPacket && fireblanket$fsc && !fireblanket$fscStarted) {
			fireblanket$enableFSCNow();
		}
	}

	@Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;handlePacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;)V", shift = At.Shift.BEFORE))
	public void fireblanket$receive(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
//		System.out.println("Receive: " + packet.getClass().getName() + " " + fireblanket$fsc + " " + fireblanket$fscStarted);
//		System.out.println("Receive: " + packet.getClass().getName()
//			+ (packet instanceof CustomPayloadS2CPacket(CustomPayload payload) ? " as " + payload.getId() : ""));

		// idk man
		if (packet instanceof DisconnectS2CPacket || packet instanceof LoginDisconnectS2CPacket) {
			fireblanket$fscStarted = false;
			fireblanket$fsc = false;
		}
	}

	@Inject(at=@At("HEAD"), method="setCompressionThreshold", cancellable=true)
	public void fireblanket$handleCompression(int threshold, boolean check, CallbackInfo ci) {
		if (fireblanket$fscStarted) {
			ci.cancel();
		}
	}

	private void fireblanket$enableFSCNow() {
//		Thread.dumpStack();

		fireblanket$fscStarted = true;
		ChannelPipeline pipeline = channel.pipeline();
		ClientConnection self = (ClientConnection)(Object)this;
		try {
			boolean client = self.getSide() == NetworkSide.CLIENTBOUND;
			ReassignableOutputStream ros = new ReassignableOutputStream();
			ZstdOutputStream zos = new ZstdOutputStream(ros);
			zos.setLevel(client ? 6 : 4);
			zos.setLong(client ? 27 : 22);
			zos.setCloseFrameOnFlush(false);
			ZstdEncoder enc = new ZstdEncoder(ros, zos, TimeUnit.MILLISECONDS.toNanos(client ? 0 : 40));
			ZstdDecoder dec = new ZstdDecoder();
			pipeline.remove("compress");
			pipeline.remove("decompress");
			pipeline.addBefore("prepender", "fireblanket:fsc_enc", enc);
			pipeline.addBefore("splitter", "fireblanket:fsc_dec", dec);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void fireblanket$enableFullStreamCompression() {
		fireblanket$fsc = true;
	}
}
