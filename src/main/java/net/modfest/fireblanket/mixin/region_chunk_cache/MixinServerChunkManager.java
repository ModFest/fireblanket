package net.modfest.fireblanket.mixin.region_chunk_cache;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * NOTE: only ever applied when fireblanket.loadRadius is specified!
 */
@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {
	@Shadow @Final Thread serverThread;

	@Shadow @Final ServerWorld world;

	@Shadow @Final private ServerChunkManager.MainThreadExecutor mainThreadExecutor;

	@Shadow protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> getChunkFuture(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create);


	@Shadow public abstract @Nullable Chunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create);

	private Chunk[] fireblanket$chunkCache;
	private ChunkStatus[] fireblanket$chunkStatusCache;
	private int fireblanket$min;
	private int fireblanket$max;
	private int fireblanket$width;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fireblanket$initData(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureTemplateManager structureTemplateManager, Executor workerExecutor, ChunkGenerator chunkGenerator, int viewDistance, int simulationDistance, boolean dsync, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier persistentStateManagerFactory, CallbackInfo ci) {
		// Will be real due to mixin plugin

		if (this.world.getRegistryKey().equals(World.OVERWORLD)) {
			int radius = Integer.getInteger("fireblanket.loadRadius");
			int min = (int) Math.floor(-radius / 16);
			int max = (int) Math.ceil(radius / 16);

			int width = (max - min) + 1;

			this.fireblanket$chunkCache = new Chunk[width * width];
			this.fireblanket$chunkStatusCache = new ChunkStatus[width * width];
			this.fireblanket$min = min;
			this.fireblanket$max = max;
			this.fireblanket$width = width;
		}
	}

	/**
	 * @author Jasmine
	 *
	 * @reason More optimal to have a cache of a given size
	 */
	@Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"), cancellable = true)
	private void fireblanket$patchGetChunk(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
		if (this.world.getRegistryKey().equals(World.OVERWORLD)) {
			// Copy of vanilla logic with custom caching
			if (Thread.currentThread() != this.serverThread) {
				Chunk c = CompletableFuture.supplyAsync(() -> this.getChunk(x, z, leastStatus, create), this.mainThreadExecutor).join();
				cir.setReturnValue(c);
			} else {
				Profiler profiler = this.world.getProfiler();
				profiler.visit("getChunk");

				int min = this.fireblanket$min;
				int max = this.fireblanket$max;

				Chunk[] cache = this.fireblanket$chunkCache;
				ChunkStatus[] scache = this.fireblanket$chunkStatusCache;
				int cacheIdx = this.fireblanket$getIndex(x, z);

				if (cacheIdx < cache.length && cacheIdx >= 0 && x >= min && x <= max && z >= min && z <= max) {
					Chunk chunk = cache[cacheIdx];

					// impl note: this is checking for /reference equality/ of the chunk status, but the param mapping
					// mentions that it's the "least status". Let's just replicate the vanilla behavior and not
					// think about it too hard.
					if (chunk != null && leastStatus == scache[cacheIdx]) {
						// cache hit!

						// Debug, but let's leave it in: the JIT will uncommon_trap this, so it shouldn't matter
						if (chunk.getPos().x != x || chunk.getPos().z != z) {
							throw new IllegalStateException("Fireblanket detected a catastrophic mismatch in its chunk cache. " +
									"Please report this to Jasmine with the following information: " + chunk.getPos() + " " + x + " " + z + " " + cacheIdx);
						}

						cir.setReturnValue(chunk);
						return;
					} else if (!create) {
						// We don't want to create a chunk, so return null
						cir.setReturnValue(null);
						return;
					}

					// miss: do the rest of the logic

					// Generate the chunk
					profiler.visit("getChunkCacheMiss");
					CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = this.getChunkFuture(x, z, leastStatus, create);
					this.mainThreadExecutor.runTasks(completableFuture::isDone);
					chunk = completableFuture.join().map(chunkx -> chunkx, unloaded -> {
						if (create) {
							throw Util.throwOrPause(new IllegalStateException("Chunk not there when requested: " + unloaded));
						} else {
							return null;
						}
					});

					// Put in the cache for next time
					cache[cacheIdx] = chunk;
					scache[cacheIdx] = leastStatus;

					cir.setReturnValue(chunk);
					return;
				}
			}
		}
	}

	private int fireblanket$getIndex(int x, int z) {
		x -= this.fireblanket$min;
		z -= this.fireblanket$min;

		return (x * (this.fireblanket$width)) + z;
	}
}
