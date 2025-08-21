package net.modfest.fireblanket.mixin.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinServerPlayNetworkHandler extends ServerCommonNetworkHandler {
	@Shadow
	public ServerPlayerEntity player;

	public MixinServerPlayNetworkHandler(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
		super(server, connection, clientData);
	}

	@Inject(method = "onUpdateCommandBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/CommandBlockExecutor;markDirty()V"))
	private void fireblanket$markUpdate(UpdateCommandBlockC2SPacket packet, CallbackInfo ci) {
		BlockPos blockPos = packet.getPos();
		BlockEntity blockEntity = this.player.getWorld().getBlockEntity(blockPos);

		this.server.sendMessage(
			Text.literal(
				"%s set the command at (%s) to: %s".formatted(
					player.getName().getString(),
					"%s, %s, %s".formatted(blockPos.getX(), blockPos.getY(), blockPos.getZ()),
					packet.getCommand()
				)
			)
		);

		if (blockEntity instanceof CommandBE cmd) {
			cmd.fireblanket$setLastUpdate(this.player.getUuid());
		}
	}
}
