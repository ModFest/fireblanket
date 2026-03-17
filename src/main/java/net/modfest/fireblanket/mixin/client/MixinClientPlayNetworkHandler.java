package net.modfest.fireblanket.mixin.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ClientPacketListener.class, priority = 10000)
public class MixinClientPlayNetworkHandler {
	// Get rid of "Received passengers for unknown entity" warning that spams logs
	@Redirect(method = "handleSetEntityPassengersPacket(Lnet/minecraft/network/protocol/game/ClientboundSetPassengersPacket;)V",
		at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V"))
	private void fireblanket$yeetPassengerError(Logger instance, String s) {
	}
}
