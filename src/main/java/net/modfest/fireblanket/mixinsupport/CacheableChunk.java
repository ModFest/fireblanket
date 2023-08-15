package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.LightData;

public interface CacheableChunk {

	public record CachedChunkPacketData(ChunkData chunkData, LightData lightData) {}
	
	CachedChunkPacketData fireblanket$getCachedPacket();
	void fireblanket$setCachedPacket(CachedChunkPacketData pkt);
	
}
