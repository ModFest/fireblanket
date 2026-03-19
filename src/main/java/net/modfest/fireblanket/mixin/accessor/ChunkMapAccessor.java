package net.modfest.fireblanket.mixin.accessor;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(ChunkMap.class)
public interface ChunkMapAccessor {
	@Accessor
	Long2ObjectLinkedOpenHashMap<ChunkHolder> getVisibleChunkMap();
}
