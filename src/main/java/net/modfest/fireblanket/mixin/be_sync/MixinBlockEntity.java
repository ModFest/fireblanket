package net.modfest.fireblanket.mixin.be_sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.world.CachedCompoundBE;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public class MixinBlockEntity implements CachedCompoundBE {
	private CompoundTag fireblanket$lastSeenCompound = null;

	@Override
	public @Nullable CompoundTag fireblanket$getCachedCompound() {
		return this.fireblanket$lastSeenCompound;
	}

	@Override
	public void fireblanket$setCachedCompound(CompoundTag nbt) {
		this.fireblanket$lastSeenCompound = nbt;
	}
}
