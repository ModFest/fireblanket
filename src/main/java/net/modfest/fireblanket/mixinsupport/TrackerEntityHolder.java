package net.modfest.fireblanket.mixinsupport;

import net.minecraft.server.network.ServerPlayerConnection;

import java.util.Set;

public interface TrackerEntityHolder {
	void fireblanket$setListeners(Set<ServerPlayerConnection> listeners);
}
