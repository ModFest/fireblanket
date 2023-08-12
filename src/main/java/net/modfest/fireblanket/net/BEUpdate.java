package net.modfest.fireblanket.net;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public record BEUpdate(BlockPos pos, BlockEntityType<?> type, NbtCompound nbt) {
}
