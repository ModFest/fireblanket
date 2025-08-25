package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.util.TextUtil;
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
	private void fireblanket$markUpdate(
		final UpdateCommandBlockC2SPacket packet,
		final CallbackInfo ci,
		final @Local CommandBlockBlockEntity blockEntity
	) {
		BlockPos blockPos = packet.getPos();

		final Text message = Text.translatableWithFallback(
			"commandsBlock.commandSetByPlayer",
			// In case someone has an outdated Fireblanket, or is lacking it outright.
			"[%s @ %s: %s]",
			player.getName(),
			TextUtil.ofLocationWithTeleport(blockEntity.getWorld(), blockPos),
			packet.getCommand()
		);

		CommandUtils.sendToTeam(this.server, this.player.getCommandOutput(), message);

		if (blockEntity instanceof CommandBE cmd) {
			cmd.fireblanket$setLastUpdate(this.player.getUuid());
		}
	}

	@Inject(
		method = "onUpdateCommandBlockMinecart",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/CommandBlockExecutor;markDirty()V")
	)
	private void fireblanket$markUpdate(
		final UpdateCommandBlockMinecartC2SPacket packet,
		final CallbackInfo ci,
		final @Local CommandBlockExecutor executor
	) {
		final int entityId = ((AccessorUpdateCommandBlockMinecartC2SPacket) packet).getEntityId();
		final Entity entity = this.player.getWorld().getEntityById(entityId);

		final Text entityName;

		if (entity == null) {
			entityName = Text.of("??? Missing entity: " + entityId);
		} else {
			entityName = TextUtil.ofEntityWithTeleport(entity);
		}

		final Text message = Text.translatableWithFallback(
			"commandsBlock.commandSetByPlayer",
			// In case someone has an outdated Fireblanket, or is lacking it outright.
			"[%s @ %s: %s]",
			player.getName(),
			entityName,
			packet.getCommand()
		);

		CommandUtils.sendToTeam(this.server, this.player.getCommandOutput(), message);

		((CommandBE) executor).fireblanket$setLastUpdate(this.player.getUuid());
	}
}
