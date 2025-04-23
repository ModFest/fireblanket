package net.modfest.fireblanket.mixin.log;

import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
	@Redirect(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;requestTeleport(DDDFF)V", ordinal = 1))
	private void fireblanket$move1(ServerPlayNetworkHandler instance, double x, double y, double z, float yaw, float pitch) {

	}

	@Redirect(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
	private void fireblanket$move2(ServerPlayNetworkHandler instance, Packet packet) {

	}

	@Redirect(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 0, remap = false))
	private void fireblanket$log1(Logger logger, String format, Object... arguments) {

	}

	@Redirect(method = "onVehicleMove", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", ordinal = 0, remap = false))
	private void fireblanket$log2(Logger logger, String format, Object... arguments) {

	}
}
