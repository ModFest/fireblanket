package net.modfest.fireblanket.mixinsupport;

import java.util.UUID;

public interface CommandBE {
	void fireblanket$setOwner(UUID uuid);
	void fireblanket$setLastUpdate(UUID uuid);

	UUID fireblanket$getOwner();
	UUID fireblanket$getLastUpdate();
}
