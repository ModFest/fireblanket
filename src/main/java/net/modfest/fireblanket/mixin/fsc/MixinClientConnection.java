package net.modfest.fireblanket.mixin.fsc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.luben.zstd.ZstdOutputStream;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.ReassignableOutputStream;
import net.modfest.fireblanket.Fireblanket.QueuedPacket;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.net.ZstdDecoder;
import net.modfest.fireblanket.net.ZstdEncoder;

@Mixin(ClientConnection.class)
public class MixinClientConnection implements FSCConnection {

	@Shadow
	private Channel channel;
	
	@Shadow
	private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks) { throw new AbstractMethodError(); }
	
	private final LinkedBlockingQueue<QueuedPacket> fireblanket$queue = Fireblanket.getNextQueue();
	private boolean fireblanket$fsc = false;
	private boolean fireblanket$fscStarted = false;
	
	/**
	 * With a lot of connections, simply the act of writing packets becomes slow.
	 * Doing this on the server thread reduces TPS for no good reason.
	 * 
	 * The client already does networking roughly like this, so the protocol stack is already
	 * designed to expect this behavior.
	 */
	@Redirect(at=@At(value="INVOKE", target="net/minecraft/network/ClientConnection.sendImmediately(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V"),
			method="send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;)V")
	public void fireblanket$asyncPacketSending(ClientConnection subject, Packet<?> pkt, PacketCallbacks listener) {
		if (pkt instanceof GameJoinS2CPacket && fireblanket$fsc && !fireblanket$fscStarted) {
			fireblanket$enableFSCNow();
		}
		if (channel.attr(ClientConnection.PROTOCOL_ATTRIBUTE_KEY).get() == NetworkState.PLAY) {
			fireblanket$queue.add(new QueuedPacket(subject, pkt, listener));
		} else {
			sendImmediately(pkt, listener);
		}
	}
	
	@Inject(at=@At("HEAD"), method="setCompressionThreshold", cancellable=true)
	public void fireblanket$handleCompression(int threshold, boolean check, CallbackInfo ci) {
		if (fireblanket$fscStarted) {
			ci.cancel();
		}
	}
	
	@Inject(at=@At("HEAD"), method="setState")
	public void fireblanket$handleFSC(NetworkState state, CallbackInfo ci) {
		if (state == NetworkState.PLAY && fireblanket$fsc && !fireblanket$fscStarted) {
			fireblanket$enableFSCNow();
		}
	}
	
	private void fireblanket$enableFSCNow() {
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
