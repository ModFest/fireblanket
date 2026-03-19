package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.network.protocol.login.ClientboundLoginFinishedPacket;
import net.minecraft.network.protocol.login.ServerboundLoginAcknowledgedPacket;
import net.modfest.fireblanket.mixin.fsc.MixinClientConfigurationNetworkHandler;
import net.modfest.fireblanket.mixin.fsc.MixinClientLoginPacketListenerImpl;
import net.modfest.fireblanket.mixin.fsc.MixinServerConfigurationNetworkHandler;
import net.modfest.fireblanket.mixin.fsc.MixinServerLoginPacketListenerImpl;

public interface FSCConnection {

	/**
	 * If agreed upon, start full-stream compression at that point.
	 * <p>
	 * Small toys, do not arbitrarily call outside of lockstep packets, as you will encounter stream bugs.
	 *
	 * @implSpec The server shall call this right after receiving a lockstep packet,
	 * 	before sending any other packets.
	 * 	The client shall call this after flushing the agreed lockstep packet.
	 * 	The method must ensure both ends agreed to FSC before starting it.
	 * @implNote This is currently called at both the {@code LOGIN} -> {@code CONFIGURATION},
	 * listening on the {@link ServerboundLoginAcknowledgedPacket ServerLoginAcknowledged}
	 * and {@link ClientboundLoginFinishedPacket ClientboundLoginFinsihed} packets.
	 * and {@code CONFIGURATION} -> {@code PLAY} transitions,
	 * listening on the {@link ServerboundFinishConfigurationPacket ServerboundFinish}
	 * and {@link ClientboundFinishConfigurationPacket ClientboundFinish} packets.
	 * @see MixinClientConfigurationNetworkHandler
	 * @see MixinServerConfigurationNetworkHandler
	 * @see MixinClientLoginPacketListenerImpl
	 * @see MixinServerLoginPacketListenerImpl
	 */
	void fireblanket$startFullStreamCompression(final long millis);

	void fireblanket$enableFullStreamCompression();

}
