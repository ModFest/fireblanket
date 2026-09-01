package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandSourceStack.class)
public interface ServerCommandSourceAccessor {
	/**
	 * Safety: bypass getDisplayName to avoid recursion
	 */
	@Accessor
	CommandSourceStack.NamesProvider getNamesProvider();

	@Accessor
	CommandSource getSource();
}
