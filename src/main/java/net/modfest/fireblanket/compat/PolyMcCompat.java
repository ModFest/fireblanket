package net.modfest.fireblanket.compat;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.modfest.fireblanket.PolyMcAccess;

public class PolyMcCompat {

    public static void init() {
        PolyMcAccess.isActive = () -> {
            ServerPlayerEntity polyPlayer = ChunkPacketStaticHack.player.get();
            return polyPlayer != null && Util.tryGetPolyMap(polyPlayer).isVanillaLikeMap();
        };
    }
    
}
