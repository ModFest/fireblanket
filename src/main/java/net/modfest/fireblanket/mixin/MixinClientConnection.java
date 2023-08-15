package net.modfest.fireblanket.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.modfest.fireblanket.Fireblanket;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

	@Shadow
	private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks) { throw new AbstractMethodError(); }
	
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
		Fireblanket.PACKET_QUEUE.add(() -> {
			sendImmediately(pkt, listener);
		});
	}
	
}
