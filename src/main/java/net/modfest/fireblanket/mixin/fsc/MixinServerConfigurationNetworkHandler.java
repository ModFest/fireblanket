package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class MixinServerConfigurationNetworkHandler extends ServerCommonNetworkHandler {

	public MixinServerConfigurationNetworkHandler(final MinecraftServer server, final ClientConnection connection, final ConnectedClientData clientData) {
		super(server, connection, clientData);
	}

	/**
	 * Inject point for FullStreamCompression.
	 *
	 * @author Ampflower
	 * @see FSCConnection#fireblanket$startFullStreamCompression()
	 **/
	@Inject(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;transitionOutbound(Lnet/minecraft/network/state/NetworkState;)V"))
	private void fireblanket$startFSC(CallbackInfo ci) {
		((FSCConnection) this.connection).fireblanket$startFullStreamCompression();
	}
}
