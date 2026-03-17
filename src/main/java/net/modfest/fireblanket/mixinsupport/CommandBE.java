package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.level.BaseCommandBlock;

import java.util.UUID;

public interface CommandBE {
	BaseCommandBlock fireblanket$getCommandExecutor();

	void fireblanket$setOwner(UUID uuid);

	void fireblanket$setLastUpdate(UUID uuid);

	UUID fireblanket$getOwner();

	UUID fireblanket$getLastUpdate();

	HoverEvent fireblanket$getBlame();
}
