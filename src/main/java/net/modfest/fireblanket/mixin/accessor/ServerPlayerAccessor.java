package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {
	@Accessor
	MinecraftServer getServer();
}
