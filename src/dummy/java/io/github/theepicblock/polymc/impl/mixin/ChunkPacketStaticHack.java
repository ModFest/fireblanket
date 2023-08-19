package io.github.theepicblock.polymc.impl.mixin;

import net.minecraft.server.network.ServerPlayerEntity;

public class ChunkPacketStaticHack {
	public static ThreadLocal<ServerPlayerEntity> player = new ThreadLocal<>();
}
