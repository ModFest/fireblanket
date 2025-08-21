package net.modfest.fireblanket.mixin.command;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.modfest.fireblanket.FireblanketConstants;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.util.OffthreadFileWriter;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


@Mixin(CommandManager.class)
public class MixinCommandManager {
	@Shadow
	@Final
	private static Logger LOGGER;

	/**
	 * Log command executions.
	 * They are filtered to only be on players.
	 *
	 * @author Luna (Awakened-Redstone)
	 */
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;callWithContext(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE), method = "execute")
	private void logPlayerCommands(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci) {
		if (!FireblanketConfig.get(ConfigSpecs.LOG_PLAYER_COMMANDS)) return;

		CommandContextBuilder<ServerCommandSource> context = parseResults.getContext();
		ServerCommandSource source = context.getSource();
		if (!source.isExecutedByPlayer()) return;

		String baseCommand = CommandUtils.findSourceNode(context.getNodes().getFirst().getNode()).getName();
		if (FireblanketConfig.get(ConfigSpecs.IGNORED_COMMAND_LOGS).contains(baseCommand)) {
			return;
		}

		CommandUtils.sendToTeam(source, Text.literal("/" + command));

		OffthreadFileWriter.write(
			// Strip all possible : from a name to make parsing the file easier if required at some point
			"[%s] [%s:%s] /%s\n".formatted(
				FireblanketConstants.SIMPLE_TIME_FORMATTER.format(LocalDateTime.now()),
				source.getName().replaceAll("[:\\[\\]]", "_"),
				source.getPlayer().getUuid(),
				command
			),
			FireblanketConstants.COMMAND_LOGS_FILE
		);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendError(Lnet/minecraft/text/Text;)V", ordinal = 0), method = "execute")
	private void logCommandErrors(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci, @Local Exception exception) {
		if (FireblanketConfig.get(ConfigSpecs.LOG_COMMAND_ERRORS)) {
			LOGGER.error("'/{}' threw an exception", command, exception);
		}
	}
}
