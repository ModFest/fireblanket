package net.modfest.fireblanket.world;

import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public interface CachedCompoundBE {
    @Nullable NbtCompound fireblanket$getCachedCompound();

    void fireblanket$setCachedCompound(NbtCompound nbt);
}
