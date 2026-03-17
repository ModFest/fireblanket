package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSourceStack.class)
public interface ServerCommandSourceAccessor {
	/**
	 * Safety: bypass getDisplayName to avoid recursion
	 */
	@Accessor("displayName")
	Component fireblanket$getRawDisplayName();

	@Accessor
	CommandSource getSource();
}
