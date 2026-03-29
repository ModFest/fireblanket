package net.modfest.fireblanket.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;
import java.util.Set;

/**
 * Allows negotiating the network state required to enable FSC.
 *
 * @author Ampflower
 * @implNote The order of the enum is load bearing, and expected to be sequential.
 * 	If this ever requires changes, please be sure to order all new enums to be <em>before</em> {@link #NONE},
 * 	and that the enums are ordered to how they'll be encountered in the network protocol.
 * 	<p>
 * 	If the entrypoint of a given enum has been moved in such a way that starting FSC is now
 * 	incompatible with the previous version, give it a new network name to prevent the old client from using it.
 **/
public enum NetworkState {
	// This would ideally be via handshake, using a special subdomain that is sent by the client,
	// if it sees a magic in the status. This would only be possible if the server has been seen before joining.
	// This can break proxies, and may be disableable for proxies.
	// However, if it is seen at login stage instead, this can be effectively be considered as enable immediately.
	// Since login runs in lockstep, we can safely swap the pipeline pieces to enable FSC.
	LOGIN("login"),
	CONFIGURATION("configuration"),
	PLAY("play"),
	// Only be possible on a vanilla client
	// A Fireblanket client sending this should be disconnected?
	// The client must refuse to acknowledge the packet if it receives this state explicitly.
	NONE("none"),
	// acts as PLAY if the server can't understand it.
	// The client must refuse to acknowledge the packet if it receives this state explicitly.
	UNKNOWN("unknown"),
	;

	public static final StreamCodec<ByteBuf, NetworkState> CODEC = ByteBufCodecs.STRING_UTF8
		.map(NetworkState::parse, NetworkState::getNetworkName);

	// TODO: configuration-based negotiation
	public static final Set<NetworkState> VALID = Set.of(LOGIN, CONFIGURATION, PLAY);

	private final String name;

	NetworkState(final String name) {
		this.name = name;
	}

	public static NetworkState parse(final String string) {
		return switch (string.toLowerCase(Locale.ROOT)) {
			case "none" -> NONE;
			case "login" -> LOGIN;
			case "configuration" -> CONFIGURATION;
			case "play" -> PLAY;
			default -> UNKNOWN;
		};
	}

	public final String getNetworkName() {
		return name;
	}
}
