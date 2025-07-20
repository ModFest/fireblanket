package net.modfest.fireblanket.mixin.packet_chunk_cache;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import net.modfest.fireblanket.mixinsupport.CacheableChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk extends Chunk implements CacheableChunk {

	public MixinWorldChunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, ChunkSection[] sectionArray, BlendingData blendingData) {
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
		method = "markNeedsSaving", at = @At("TAIL")
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
