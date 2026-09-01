package net.modfest.fireblanket.mixin.log;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerPlayNetworkHandler {
	@Redirect(
		method = "handlePlayerPositionChange",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V",
			ordinal = 1
		)
	)
	private void fireblanket$move1(ServerGamePacketListenerImpl instance, double x, double y, double z, float yaw, float pitch) {

	}

	@Redirect(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
	private void fireblanket$move2(ServerGamePacketListenerImpl instance, Packet packet) {

	}

	@Redirect(
		method = "handlePlayerPositionChange",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V",
			ordinal = 0,
			remap = false
		)
	)
	private void fireblanket$log1(Logger logger, String format, Object... arguments) {

	}

	@Redirect(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 0, remap = false))
	private void fireblanket$log2(Logger logger, String format, Object... arguments) {

	}
}
