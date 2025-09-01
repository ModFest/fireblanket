package net.modfest.fireblanket.mixin.command;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.modfest.fireblanket.FireblanketConstants;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.util.OffthreadFileWriter;
import net.modfest.fireblanket.util.TextUtil;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.MessageFormat;
import java.time.LocalDateTime;


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

		Text message = Text.translatable("chat.type.admin", TextUtil.ofRunner(source), Text.literal("/" + command));

		CommandUtils.sendToTeam(source, message);

		OffthreadFileWriter.write(
			toLog(source, command),
			FireblanketConstants.COMMAND_LOGS_FILE
		);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;sendError(Lnet/minecraft/text/Text;)V", ordinal = 0), method = "execute")
	private void logCommandErrors(ParseResults<ServerCommandSource> parseResults, String command, CallbackInfo ci, @Local Exception exception) {
		if (FireblanketConfig.get(ConfigSpecs.LOG_COMMAND_ERRORS)) {
			LOGGER.error("'/{}' threw an exception", command, exception);
		}
	}

	/**
	 * Transforms the given command source and command string into a log entry.
	 */
	@Unique
	private static String toLog(final ServerCommandSource source, final String command) {
		final GameProfile sourceProfile = source.getPlayer().getGameProfile();
		final ServerPlayerEntity runner = CommandUtils.getPlayerRunner(source);
		final String time = FireblanketConstants.SIMPLE_TIME_FORMATTER.format(LocalDateTime.now());

		if (runner == source.getEntity()) {
			return MessageFormat.format("[{0}] [\0{1}\0:{2}]: /{3}\n",
				time,
				sourceProfile.getName().replace('\0', '�'),
				sourceProfile.getId(),
				command
			);
		}

		if (runner == null) {
			return MessageFormat.format("[{0}] [\0{1}\0 \uD83C\uDFAD \0{2}\0:{3}]: /{4}\n",
				time,
				// FIXME:
				source.getName().replace('\0', '�'),
				sourceProfile.getName().replace('\0', '�'),
				sourceProfile.getId(),
				command
			);
		}

		final GameProfile runnerProfile = runner.getGameProfile();

		return MessageFormat.format(
			"[{0}] [\0{1}\0:{2} \uD83C\uDFAD \0{3}\0:{4}]: /{5}\n",
			time,
			runnerProfile.getName().replace('\0', '�'),
			runnerProfile.getId(),
			sourceProfile.getName().replace('\0', '�'),
			sourceProfile.getId(),
			command
		);
	}
}
