package net.modfest.fireblanket.mixin.command;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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


@Mixin(Commands.class)
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
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V", shift = At.Shift.BEFORE), method = "performCommand")
	private void logPlayerCommands(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
		if (!FireblanketConfig.get(ConfigSpecs.LOG_PLAYER_COMMANDS)) return;

		CommandContextBuilder<CommandSourceStack> context = parseResults.getContext();
		CommandSourceStack source = context.getSource();
		if (!source.isPlayer()) return;

		String baseCommand = CommandUtils.findSourceNode(context.getNodes().getFirst().getNode()).getName();
		if (FireblanketConfig.get(ConfigSpecs.IGNORED_COMMAND_LOGS).contains(baseCommand)) {
			return;
		}

		Component message = Component.translatable("chat.type.admin", TextUtil.ofRunner(source), Component.literal("/" + command));

		CommandUtils.sendToTeam(source, message);

		OffthreadFileWriter.write(
			toLog(source, command),
			FireblanketConstants.COMMAND_LOGS_FILE
		);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;sendFailure(Lnet/minecraft/network/chat/Component;)V", ordinal = 0), method = "performCommand")
	private void logCommandErrors(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci, @Local Exception exception) {
		if (FireblanketConfig.get(ConfigSpecs.LOG_COMMAND_ERRORS)) {
			LOGGER.error("'/{}' threw an exception", command, exception);
		}
	}

	/**
	 * Transforms the given command source and command string into a log entry.
	 */
	@Unique
	private static String toLog(final CommandSourceStack source, final String command) {
		final GameProfile sourceProfile = source.getPlayer().getGameProfile();
		final ServerPlayer runner = CommandUtils.getPlayerRunner(source);
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
				source.getTextName().replace('\0', '�'),
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
