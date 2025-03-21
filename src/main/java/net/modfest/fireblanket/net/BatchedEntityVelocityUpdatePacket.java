package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.modfest.fireblanket.Fireblanket;

import java.util.ArrayList;
import java.util.List;

public record BatchedEntityVelocityUpdatePacket(List<VelocityUpdate> updates) implements CustomPayload {
	public static final CustomPayload.Id<BatchedEntityVelocityUpdatePacket> ID = new CustomPayload.Id<>(Fireblanket.BATCHED_VELOCITY_SYNC);

	public static final PacketCodec<RegistryByteBuf, BatchedEntityVelocityUpdatePacket> CODEC = PacketCodec.tuple(
		PacketCodecs.collection(ArrayList::new, VelocityUpdate.CODEC),
		BatchedEntityVelocityUpdatePacket::updates,
		BatchedEntityVelocityUpdatePacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
