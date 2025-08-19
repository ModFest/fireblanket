package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.network.ClientConnection;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(ClientConfigurationNetworkHandler.class)
public abstract class MixinClientConfigurationNetworkHandler extends ClientCommonNetworkHandler {

	protected MixinClientConfigurationNetworkHandler(final MinecraftClient client, final ClientConnection connection, final ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	/**
	 * Inject point for FullStreamCompression.
	 *
	 * @author Ampflower
	 * @see FSCConnection#fireblanket$startFullStreamCompression()
	 **/
	@Inject(method = "onReady", at = @At("RETURN"))
	private void fireblanket$startFSC(CallbackInfo ci) {
		((FSCConnection) this.connection).fireblanket$startFullStreamCompression();
	}
}
