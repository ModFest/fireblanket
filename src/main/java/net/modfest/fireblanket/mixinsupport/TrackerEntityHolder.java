package net.modfest.fireblanket.mixinsupport;

import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.Set;

public interface TrackerEntityHolder {
	void setListeners(Set<PlayerAssociatedNetworkHandler> listeners);
}
