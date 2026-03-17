package net.modfest.fireblanket.mixin.packet_chunk_cache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.modfest.fireblanket.mixinsupport.CacheableChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class MixinWorldChunk extends ChunkAccess implements CacheableChunk {

	public MixinWorldChunk(ChunkPos pos, UpgradeData upgradeData, LevelHeightAccessor heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, LevelChunkSection[] sectionArray, BlendingData blendingData) {
		super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
	}

	private CachedChunkPacketData fireblanket$cachedPacket;

	@Inject(at = @At("RETURN"), method = "setBlockState")
	public void fireblanket$invalidateOnSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
		fireblanket$cachedPacket = null;
	}

	@Inject(at = @At("RETURN"), method = "removeBlockEntity")
	public void fireblanket$invalidateOnRemoveBlockEntity(BlockPos pos, CallbackInfo ci) {
		fireblanket$cachedPacket = null;
	}

	@Inject(at = @At("RETURN"), method = "setBlockEntity")
	public void fireblanket$invalidateOnSetBlockEntity(BlockEntity be, CallbackInfo ci) {
		fireblanket$cachedPacket = null;
	}

	@Inject(
		method = "markUnsaved", at = @At("TAIL")
	) private void markNeedsSaving(CallbackInfo ci) {
		fireblanket$cachedPacket = null;
	}

	@Override
	public CachedChunkPacketData fireblanket$getCachedPacket() {
		return fireblanket$cachedPacket;
	}

	@Override
	public void fireblanket$setCachedPacket(CachedChunkPacketData pkt) {
		fireblanket$cachedPacket = pkt;
	}

}
