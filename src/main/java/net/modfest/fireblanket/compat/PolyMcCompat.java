package net.modfest.fireblanket.compat;

import io.github.theepicblock.polymc.impl.Util;
import io.github.theepicblock.polymc.impl.mixin.ChunkPacketStaticHack;
import net.minecraft.server.level.ServerPlayer;

public class PolyMcCompat {

	public static void init() {
		PolyMcAccess.isActive = () -> {
			ServerPlayer polyPlayer = ChunkPacketStaticHack.player.get();
			return polyPlayer != null && Util.tryGetPolyMap(polyPlayer).isVanillaLikeMap();
		};
	}

}
