package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import net.modfest.fireblanket.net.NetworkState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class MixinServerConfigurationNetworkHandler extends ServerCommonPacketListenerImpl {

	public MixinServerConfigurationNetworkHandler(final MinecraftServer server, final Connection connection, final CommonListenerCookie clientData) {
		super(server, connection, clientData);
	}

	/**
	 * Inject point for FullStreamCompression.
	 *
	 * @author Ampflower
	 **/
	@Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"))
	private void fireblanket$startFSC(CallbackInfo ci) {
		((FSCConnection) this.connection).fireblanket$startFullStreamCompression(NetworkState.PLAY, 40L);
	}
}
