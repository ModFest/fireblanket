package net.modfest.fireblanket.mixin.packet_chunk_cache;

import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.modfest.fireblanket.compat.PolyMcAccess;
import net.modfest.fireblanket.mixinsupport.CacheableChunk;
import net.modfest.fireblanket.mixinsupport.CacheableChunk.CachedChunkPacketData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public class MixinChunkDataS2CPacket {

	@Shadow
	@Final
	private ClientboundLevelChunkPacketData chunkData;
	@Shadow
	@Final
	private ClientboundLightUpdatePacketData lightData;

	@Redirect(at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundLevelChunkPacketData"),
		method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V")
	private static ClientboundLevelChunkPacketData fireblanket$useCachedChunkData(LevelChunk chunk) {
		if (!PolyMcAccess.isActive() && chunk instanceof CacheableChunk cc) {
			CachedChunkPacketData data = cc.fireblanket$getCachedPacket();
			if (data != null) {
				return data.chunkData();
			}
		}
		return new ClientboundLevelChunkPacketData(chunk);
	}

	@Redirect(at = @At(value = "NEW", target = "net/minecraft/network/protocol/game/ClientboundLightUpdatePacketData"),
		method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V")
	private static ClientboundLightUpdatePacketData fireblanket$useCachedLightData(
		ChunkPos pos,
		LevelLightEngine lightProvider,
		BitSet skyBits,
		BitSet blockBits,
		LevelChunk chunk
	) {
		if (!PolyMcAccess.isActive() && chunk instanceof CacheableChunk cc) {
			CachedChunkPacketData data = cc.fireblanket$getCachedPacket();
			if (data != null) {
				return data.lightData();
			}
		}
		return new ClientboundLightUpdatePacketData(pos, lightProvider, skyBits, blockBits);
	}

	@Inject(at = @At("TAIL"),
		method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V")
	public void fireblanket$saveCachedData(LevelChunk chunk, LevelLightEngine light, BitSet a, BitSet b, CallbackInfo ci) {
		if (!PolyMcAccess.isActive() && chunk instanceof CacheableChunk cc) {
			CachedChunkPacketData data = cc.fireblanket$getCachedPacket();
			if (data == null) {
				cc.fireblanket$setCachedPacket(new CachedChunkPacketData(chunkData, lightData));
			}
		}
	}

}
