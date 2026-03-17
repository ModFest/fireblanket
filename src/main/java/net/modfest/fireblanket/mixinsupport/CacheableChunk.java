package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;

public interface CacheableChunk {

	record CachedChunkPacketData(ClientboundLevelChunkPacketData chunkData,
								 ClientboundLightUpdatePacketData lightData) {
	}

	CachedChunkPacketData fireblanket$getCachedPacket();

	void fireblanket$setCachedPacket(CachedChunkPacketData pkt);

}
