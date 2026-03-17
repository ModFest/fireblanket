package net.modfest.fireblanket.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.util.TextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerPlayNetworkHandler extends ServerCommonPacketListenerImpl {
	@Shadow
	public ServerPlayer player;

	public MixinServerPlayNetworkHandler(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
		super(server, connection, clientData);
	}

	@Inject(method = "handleSetCommandBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseCommandBlock;onUpdated()V"))
	private void fireblanket$markUpdate(
		final ServerboundSetCommandBlockPacket packet,
		final CallbackInfo ci,
		final @Local CommandBlockEntity blockEntity
	) {
		BlockPos blockPos = packet.getPos();

		final Component message = Component.translatableWithFallback(
			"commandsBlock.commandSetByPlayer",
			// In case someone has an outdated Fireblanket, or is lacking it outright.
			"[%s @ %s: %s]",
			player.getName(),
			TextUtil.ofLocationWithTeleport(blockEntity.getLevel(), blockPos),
			packet.getCommand()
		);

		CommandUtils.sendToTeam(this.server, this.player.commandSource(), message);

		if (blockEntity instanceof CommandBE cmd) {
			cmd.fireblanket$setLastUpdate(this.player.getUUID());
		}
	}

	@Inject(
		method = "handleSetCommandMinecart",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseCommandBlock;onUpdated()V")
	)
	private void fireblanket$markUpdate(
		final ServerboundSetCommandMinecartPacket packet,
		final CallbackInfo ci,
		final @Local BaseCommandBlock executor
	) {
		final int entityId = ((AccessorUpdateCommandBlockMinecartC2SPacket) packet).getEntity();
		final Entity entity = this.player.level().getEntity(entityId);

		final Component entityName;

		if (entity == null) {
			entityName = Component.nullToEmpty("??? Missing entity: " + entityId);
		} else {
			entityName = TextUtil.ofEntityWithTeleport(entity);
		}

		final Component message = Component.translatableWithFallback(
			"commandsBlock.commandSetByPlayer",
			// In case someone has an outdated Fireblanket, or is lacking it outright.
			"[%s @ %s: %s]",
			player.getName(),
			entityName,
			packet.getCommand()
		);

		CommandUtils.sendToTeam(this.server, this.player.commandSource(), message);

		((CommandBE) executor).fireblanket$setLastUpdate(this.player.getUUID());
	}
}
