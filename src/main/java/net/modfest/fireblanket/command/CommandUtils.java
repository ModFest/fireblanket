package net.modfest.fireblanket.command;

import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixin.accessor.ServerCommandSourceAccessor;
import net.modfest.fireblanket.util.ReflectionUtil;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class CommandUtils {
	private static final Logger logger = LogUtils.getLogger();

	public static final DynamicCommandExceptionType GENERIC_EXCEPTION = new DynamicCommandExceptionType(message -> (Component) message);

	public static <S> CommandNode<S> findSourceNode(CommandNode<S> commandNode) {
		if (commandNode.getRedirect() != null) {
			return findSourceNode(commandNode.getRedirect());
		}

		return commandNode;
	}

	public static Entity getEntityRunner(final CommandSourceStack source) {
		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		if (output instanceof MinecartCommandBlock.MinecartCommandBase executor) {
			return executor.getMinecart();
		}

		// There's really not another branch here...
		return getPlayerRunner(source);
	}

	/**
	 * Fetches the true command runner if the player exists.
	 */
	public static ServerPlayer getPlayerRunner(final CommandSourceStack source) {
		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		// If someone backports or does something weird, short circuit.
		if (output instanceof ServerPlayer player) {
			return player;
		}

		try {
			// WARNING: this may change between versions.
			// Handle with care when porting.
			return ReflectionUtil.getHostStrict(output, ServerPlayer.class);
		} catch (IllegalAccessException | IllegalArgumentException iae) {
			// This shall not be allowed to crash, log it instead.
			logger.error("Someone made an error. Did someone miss me while porting? source: {}, output: {}", source, output, iae);
		}
		return null;
	}

	/**
	 * Protected command runner fetcher
	 */
	public static ServerPlayer getPlayerIfRunner(final CommandSourceStack source) {
		final ServerPlayer player = source.getPlayer();

		if (player == null) {
			// execute as @e
			return null;
		}

		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();
		if (player.commandSource() != output) {
			// execute as organiser
			return null;
		}

		return player;
	}

	public static boolean isPlayerRunner(final CommandSourceStack source) {
		return getPlayerIfRunner(source) != null;
	}

	public static boolean isRunner(final CommandSourceStack source) {
		return source.getEntity() == getEntityRunner(source);
	}

	public static boolean isConsole(final CommandSourceStack source) {
		if (source.getEntity() != null) {
			return false;
		}

		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		return source.getServer() == output;
	}

	public static boolean isCommandBlock(final CommandSourceStack source) {
		if (source.getEntity() != null) {
			return false;
		}

		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		return output instanceof BaseCommandBlock;
	}

	public static boolean isNetAdmin(final CommandSourceStack source) {
		return isConsole(source) || Roles.isNetadmin(getPlayerIfRunner(source));
	}

	public static boolean isOrganizer(final CommandSourceStack source) {
		return isConsole(source) || Roles.isOrganizer(getPlayerIfRunner(source));
	}

	public static boolean isBuilder(final CommandSourceStack source) {
		return isConsole(source) || Roles.isBuilder(getPlayerIfRunner(source));
	}

	public static boolean shouldReceiveBroadcast(final ServerPlayer player, final CommandSource output) {
		if (player.commandSource() == output) {
			return false;
		}

		if (player.getPermissionLevel() >= 4) {
			return true;
		}

		return Roles.isOrganizer(player);
	}

	public static void sendToTeam(final CommandSourceStack source, final Component message) {
		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		sendToTeam(source.getServer(), output, message);
	}

	public static void sendToTeam(final MinecraftServer server, final @Nullable CommandSource output, final Component message) {
		final Component text = message.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (shouldReceiveBroadcast(player, output)) {
				player.sendSystemMessage(text);
			}
		}

		if (output != server) {
			server.sendSystemMessage(text);
		}
	}
}
