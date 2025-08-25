package net.modfest.fireblanket.mixinsupport;

import net.minecraft.world.CommandBlockExecutor;

import java.util.UUID;

public interface CommandBE {
	CommandBlockExecutor fireblanket$getCommandExecutor();

	void fireblanket$setOwner(UUID uuid);

	void fireblanket$setLastUpdate(UUID uuid);

	UUID fireblanket$getOwner();

	UUID fireblanket$getLastUpdate();
}
