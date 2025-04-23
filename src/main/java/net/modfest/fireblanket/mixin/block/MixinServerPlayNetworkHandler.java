package net.modfest.fireblanket.mixin.block;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onUpdateCommandBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/CommandBlockExecutor;markDirty()V"))
	private void fireblanket$markUpdate(UpdateCommandBlockC2SPacket packet, CallbackInfo ci) {
		BlockPos blockPos = packet.getPos();
		BlockEntity blockEntity = this.player.getWorld().getBlockEntity(blockPos);

		if (blockEntity instanceof CommandBE cmd) {
			cmd.fireblanket$setLastUpdate(this.player.getUuid());
		}
	}
}
