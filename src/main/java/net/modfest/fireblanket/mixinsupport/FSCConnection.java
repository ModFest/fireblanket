package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;

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
	 * @implNote This is currently called at the {@code CONFIGURATION} -> {@code PLAY} transition,
	 * 	listening on the {@link ServerboundFinishConfigurationPacket ReadyC2S} and {@link ClientboundFinishConfigurationPacket ReadyS2C} packets.
	 * @see net.modfest.fireblanket.mixin.fsc.MixinClientConfigurationNetworkHandler
	 * @see net.modfest.fireblanket.mixin.fsc.MixinServerConfigurationNetworkHandler
	 */
	void fireblanket$startFullStreamCompression();

	void fireblanket$enableFullStreamCompression();

}
