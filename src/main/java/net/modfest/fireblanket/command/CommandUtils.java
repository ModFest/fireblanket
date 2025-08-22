package net.modfest.fireblanket.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixin.accessor.ServerCommandSourceAccessor;
import org.jetbrains.annotations.Nullable;

public final class CommandUtils {
	public static final DynamicCommandExceptionType GENERIC_EXCEPTION = new DynamicCommandExceptionType(message -> (Text) message);

	public static <S> CommandNode<S> findSourceNode(CommandNode<S> commandNode) {
		if (commandNode.getRedirect() != null) {
			return findSourceNode(commandNode.getRedirect());
		}

		return commandNode;
	}

	/**
	 * Protected command runner fetcher
	 */
	public static ServerPlayerEntity getPlayerIfRunner(final ServerCommandSource source) {
		final ServerPlayerEntity player = source.getPlayer();

		if (player == null) {
			// execute as @e
			return null;
		}

		final CommandOutput output = ((ServerCommandSourceAccessor) source).getOutput();
		if (player.getCommandOutput() != output) {
			// execute as organiser
			return null;
		}

		return player;
	}

	public static boolean isRunner(final ServerCommandSource source) {
		return getPlayerIfRunner(source) != null;
	}

	public static boolean isConsole(final ServerCommandSource source) {
		if (source.getEntity() != null) {
			return false;
		}

		final CommandOutput output = ((ServerCommandSourceAccessor) source).getOutput();

		return source.getServer() == output;
	}

	public static boolean isNetAdmin(final ServerCommandSource source) {
		return isConsole(source) || Roles.isNetadmin(getPlayerIfRunner(source));
	}

	public static boolean isOrganizer(final ServerCommandSource source) {
		return isConsole(source) || Roles.isOrganizer(getPlayerIfRunner(source));
	}

	public static boolean isBuilder(final ServerCommandSource source) {
		return isConsole(source) || Roles.isBuilder(getPlayerIfRunner(source));
	}

	public static boolean shouldReceiveBroadcast(final ServerPlayerEntity player, final CommandOutput output) {
		if (player.getCommandOutput() == output) {
			return false;
		}

		if (player.getPermissionLevel() >= 4) {
			return true;
		}

		return Roles.isOrganizer(player);
	}

	public static void sendToTeam(final ServerCommandSource source, final Text message) {
		final CommandOutput output = ((ServerCommandSourceAccessor) source).getOutput();

		sendToTeam(source.getServer(), output, message);
	}

	public static void sendToTeam(final MinecraftServer server, final @Nullable CommandOutput output, final Text message) {
		final Text text = message.copy().formatted(Formatting.GRAY, Formatting.ITALIC);

		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			if (shouldReceiveBroadcast(player, output)) {
				player.sendMessage(text);
			}
		}

		if (output != server) {
			server.sendMessage(text);
		}
	}
}
