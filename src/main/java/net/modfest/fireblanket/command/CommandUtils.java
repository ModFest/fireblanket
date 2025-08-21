package net.modfest.fireblanket.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.text.Text;

public final class CommandUtils {
	public static final DynamicCommandExceptionType GENERIC_EXCEPTION = new DynamicCommandExceptionType(message -> (Text) message);

	public static <S> CommandNode<S> findSourceNode(CommandNode<S> commandNode) {
		if (commandNode.getRedirect() != null) {
			return findSourceNode(commandNode.getRedirect());
		}

		return commandNode;
	}
}
