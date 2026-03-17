package net.modfest.fireblanket.net;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record BEUpdate(BlockPos pos, BlockEntityType<?> type, CompoundTag nbt) {
	public static final StreamCodec<RegistryFriendlyByteBuf, BEUpdate> CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		BEUpdate::pos,
		ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE),
		BEUpdate::type,
		ByteBufCodecs.TRUSTED_COMPOUND_TAG,
		BEUpdate::nbt,
		BEUpdate::new
	);
}
