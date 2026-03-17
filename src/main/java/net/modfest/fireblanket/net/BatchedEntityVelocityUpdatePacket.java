package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.modfest.fireblanket.Fireblanket;

import java.util.ArrayList;
import java.util.List;

public record BatchedEntityVelocityUpdatePacket(List<VelocityUpdate> updates) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<BatchedEntityVelocityUpdatePacket> ID = new CustomPacketPayload.Type<>(Fireblanket.BATCHED_VELOCITY_SYNC);

	public static final StreamCodec<RegistryFriendlyByteBuf, BatchedEntityVelocityUpdatePacket> CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ArrayList::new, VelocityUpdate.CODEC),
		BatchedEntityVelocityUpdatePacket::updates,
		BatchedEntityVelocityUpdatePacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
