package net.modfest.fireblanket.net;

import net.minecraft.server.network.ServerPlayerConnection;

import java.util.Set;

public record ProtoVelocityUpdate(Set<ServerPlayerConnection> listeners, VelocityUpdate update) {
}
