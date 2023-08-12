package net.modfest.fireblanket.mixin.improved_be_sync;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.modfest.fireblanket.world.CachedCompoundBE;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements CachedCompoundBE {
    private NbtCompound fireblanket$lastSeenCompound = null;

    @Override
    public @Nullable NbtCompound fireblanket$getCachedCompound() {
        return this.fireblanket$lastSeenCompound;
    }

    @Override
    public void fireblanket$setCachedCompound(NbtCompound nbt) {
        this.fireblanket$lastSeenCompound = nbt;
    }
}
