package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.server.level.ServerPlayer;

public class ChunkPacketStaticHack {
	public static ThreadLocal<ServerPlayer> player = new ThreadLocal<>();
}
