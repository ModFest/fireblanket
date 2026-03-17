package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.modfest.fireblanket.Fireblanket;

import java.util.ArrayList;
import java.util.List;

public record BatchedBEUpdatePayload(List<BEUpdate> updates) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<BatchedBEUpdatePayload> ID = new CustomPacketPayload.Type<>(Fireblanket.BATCHED_BE_UPDATE);
	public static final StreamCodec<RegistryFriendlyByteBuf, BatchedBEUpdatePayload> CODEC = StreamCodec.composite(
		ByteBufCodecs.collection(ArrayList::new, BEUpdate.CODEC),
		BatchedBEUpdatePayload::updates,
		BatchedBEUpdatePayload::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return ID;
	}
}
