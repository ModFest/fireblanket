package net.modfest.fireblanket.mixin.packet_chunk_cache;

import java.util.BitSet;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightData;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.modfest.fireblanket.PolyMcAccess;
import net.modfest.fireblanket.mixinsupport.CacheableChunk;
import net.modfest.fireblanket.mixinsupport.CacheableChunk.CachedChunkPacketData;

@Mixin(ChunkDataS2CPacket.class)
public class MixinChunkDataS2CPacket {
	
	@Shadow @Final
	private ChunkData chunkData;
	@Shadow @Final
	private LightData lightData;

	@Redirect(at=@At(value="NEW", target="net/minecraft/network/packet/s2c/play/ChunkData"),
			method="<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)V")
	public ChunkData fireblanket$useCachedChunkData(WorldChunk chunk) {
		if (!PolyMcAccess.isActive() && chunk instanceof CacheableChunk cc) {
			CachedChunkPacketData data = cc.fireblanket$getCachedPacket();
			if (data != null) {
				return data.chunkData();
			}
		}
		return new ChunkData(chunk);
	}
	
	@Redirect(at=@At(value="NEW", target="net/minecraft/network/packet/s2c/play/LightData"),
			method="<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)V")
	public LightData fireblanket$useCachedLightData(ChunkPos pos, LightingProvider lightProvider, BitSet skyBits, BitSet blockBits, WorldChunk chunk) {
		if (!PolyMcAccess.isActive() && chunk instanceof CacheableChunk cc) {
			CachedChunkPacketData data = cc.fireblanket$getCachedPacket();
			if (data != null) {
				return data.lightData();
			}
		}
		return new LightData(pos, lightProvider, skyBits, blockBits);
	}
	
	@Inject(at=@At("TAIL"),
			method="<init>(Lnet/minecraft/world/chunk/WorldChunk;Lnet/minecraft/world/chunk/light/LightingProvider;Ljava/util/BitSet;Ljava/util/BitSet;)V")
	public void fireblanket$saveCachedData(WorldChunk chunk, LightingProvider light, BitSet a, BitSet b, CallbackInfo ci) {
		if (!PolyMcAccess.isActive() && chunk instanceof CacheableChunk cc) {
			CachedChunkPacketData data = cc.fireblanket$getCachedPacket();
			if (data == null) {
				cc.fireblanket$setCachedPacket(new CachedChunkPacketData(chunkData, lightData));
			}
		}
	}
	
}
