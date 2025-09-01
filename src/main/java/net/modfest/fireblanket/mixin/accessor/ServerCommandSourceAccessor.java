package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerCommandSource.class)
public interface ServerCommandSourceAccessor {
	/**
	 * Safety: bypass getDisplayName to avoid recursion
	 */
	@Accessor("displayName")
	Text fireblanket$getRawDisplayName();

	@Accessor
	CommandOutput getOutput();
}
