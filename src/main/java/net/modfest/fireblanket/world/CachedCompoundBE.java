package net.modfest.fireblanket.world;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface CachedCompoundBE {
	@Nullable
	CompoundTag fireblanket$getCachedCompound();

	void fireblanket$setCachedCompound(CompoundTag nbt);
}
