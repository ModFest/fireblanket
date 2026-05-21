package net.modfest.fireblanket.mixin.client.accessor;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author Ampflower
 **/
@Mixin(ClientChunkCache.class)
public interface AccessorClientChunkCache {
	@Accessor
	ClientChunkCache.Storage getStorage();

	@Mixin(ClientChunkCache.Storage.class)
	interface Storage {
		@Accessor
		AtomicReferenceArray<@Nullable LevelChunk> getChunks();
	}
}
