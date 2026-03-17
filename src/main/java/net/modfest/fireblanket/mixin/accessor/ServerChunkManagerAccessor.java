package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.TicketStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkCache.class)
public interface ServerChunkManagerAccessor {

	@Accessor("ticketStorage")
	TicketStorage fireblanket$getTicketManager();

}
