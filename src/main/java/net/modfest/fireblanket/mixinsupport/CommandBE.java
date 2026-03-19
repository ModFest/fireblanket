package net.modfest.fireblanket.mixinsupport;

import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseCommandBlock;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface CommandBE {
	BaseCommandBlock fireblanket$getCommandExecutor();

	default @Nullable Entity fireblanket$getEntity() {
		return null;
	}

	void fireblanket$setOwner(@Nullable UUID uuid);

	void fireblanket$setLastUpdate(@Nullable UUID uuid);

	@Nullable UUID fireblanket$getOwner();

	@Nullable UUID fireblanket$getLastUpdate();

	HoverEvent fireblanket$getBlame();
}
