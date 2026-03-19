package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.modfest.fireblanket.FireblanketConstants;

public record CommandBlockPacket() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<CommandBlockPacket> ID =
		new CustomPacketPayload.Type<>(FireblanketConstants.id("place_command_block"));
	public static final CommandBlockPacket INST = new CommandBlockPacket();
	public static final StreamCodec<RegistryFriendlyByteBuf, CommandBlockPacket> CODEC = StreamCodec.unit(INST);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
