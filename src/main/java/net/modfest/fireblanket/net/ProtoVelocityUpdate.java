package net.modfest.fireblanket.net;

import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.Set;

public record ProtoVelocityUpdate(Set<PlayerAssociatedNetworkHandler> listeners, VelocityUpdate update) {
}
