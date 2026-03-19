package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.modfest.fireblanket.mixinsupport.FSCConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(ClientHandshakePacketListenerImpl.class)
public class MixinClientLoginPacketListenerImpl {

	@Shadow
	@Final
	private Connection connection;

	/**
	 * Inject point for FullStreamCompression.
	 *
	 * @author Ampflower
	 **/
	@Inject(
		method = "handleLoginFinished",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/Connection;setupOutboundProtocol(Lnet/minecraft/network/ProtocolInfo;)V"
		)
	)
	private void fireblanket$startFSC(CallbackInfo ci) {
		((FSCConnection) this.connection).fireblanket$startFullStreamCompression(0L);
	}
}
