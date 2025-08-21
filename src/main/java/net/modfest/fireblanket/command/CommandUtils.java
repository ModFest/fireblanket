package net.modfest.fireblanket.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixin.accessor.ServerCommandSourceAccessor;

public final class CommandUtils {
	public static final DynamicCommandExceptionType GENERIC_EXCEPTION = new DynamicCommandExceptionType(message -> (Text) message);

	public static <S> CommandNode<S> findSourceNode(CommandNode<S> commandNode) {
		if (commandNode.getRedirect() != null) {
			return findSourceNode(commandNode.getRedirect());
		}

		return commandNode;
	}

	public static boolean isOrganizer(ServerCommandSource source) {
		return source.hasPermissionLevel(4) || Roles.isOrganizer(source.getPlayer());
	}

	public static void sendToTeam(ServerCommandSource source, Text message) {
		Text text = Text.translatable("chat.type.admin", source.getDisplayName(), message).formatted(Formatting.GRAY, Formatting.ITALIC);
		CommandOutput output = ((ServerCommandSourceAccessor) source).getOutput();
		for (ServerPlayerEntity serverPlayerEntity : source.getServer().getPlayerManager().getPlayerList()) {
			if (serverPlayerEntity.getCommandOutput() != output && isOrganizer(source)) {
				serverPlayerEntity.sendMessage(text);
			}
		}

		if (output != source.getServer()) {
			source.getServer().sendMessage(text);
		}

	}
}
