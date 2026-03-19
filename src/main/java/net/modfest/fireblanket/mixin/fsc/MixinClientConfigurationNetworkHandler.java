package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class MixinClientConfigurationNetworkHandler extends ClientCommonPacketListenerImpl {

	protected MixinClientConfigurationNetworkHandler(final Minecraft client, final Connection connection, final CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}

	/**
	 * Inject point for FullStreamCompression.
	 *
	 * @author Ampflower
	 **/
	@Inject(method = "handleConfigurationFinished", at = @At("RETURN"))
	private void fireblanket$startFSC(CallbackInfo ci) {
		((FSCConnection) this.connection).fireblanket$startFullStreamCompression(0L);
	}
}
