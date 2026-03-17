package net.modfest.fireblanket.mixin.opto;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.BaseCommandBlock;
import net.modfest.fireblanket.FireblanketConstants;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.SoftReference;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * @author Ampflower
 **/
@Mixin(value = BaseCommandBlock.class, priority = 1100)
public class MixinCommandBlockExecutor {
	@Unique
	private String fireblanket$key$command;
	@Unique
	private String fireblanket$cache$command;
	@Unique
	private SoftReference<ParseResults<CommandSourceStack>> fireblanket$cache$parseResults;

	@Unique
	private Instant fireblanket$lastOutput$time;

	@Shadow
	private Component lastOutput;
	@Shadow
	private boolean trackOutput;
	@Shadow
	private String command;

	@Redirect(
		method = "performCommand",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V")
	)
	private void fireblanket$executeCached(
		final Commands self,
		final CommandSourceStack source,
		final String command
	) {
		final ParseResults<CommandSourceStack> parseResults = fireblanket$fetchCachedResults(self, source, command);

		self.performCommand(parseResults, this.fireblanket$cache$command);
	}

	@Inject(
		method = "*",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/level/BaseCommandBlock;command:Ljava/lang/String;",
			opcode = Opcodes.PUTFIELD,
			shift = At.Shift.AFTER
		)
	)
	private void fireblanket$onSetCommand(final CallbackInfo ci) {
		if (StringUtil.isNullOrEmpty(this.command) || !this.command.equals(this.fireblanket$key$command)) {
			this.fireblanket$cache$parseResults = null;
		}
		this.fireblanket$key$command = this.command;
	}

	/**
	 * @author Ampflower
	 * @reason Avoid needlessly calling {@link #fireblanket$lazy$lastOutput(Component)} twice.
	 */
	@Overwrite
	public Component getLastOutput() {
		final Component text = this.lastOutput;
		if (text == null) {
			return CommonComponents.EMPTY;
		}
		return fireblanket$lazy$lastOutput(text);
	}

	@ModifyExpressionValue(
		method = "*",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/level/BaseCommandBlock;lastOutput:Lnet/minecraft/network/chat/Component;",
			opcode = Opcodes.GETFIELD
		)
	)
	private Component fireblanket$lazy$lastOutput(final Component text) {
		final Instant time = this.fireblanket$lastOutput$time;
		this.fireblanket$lastOutput$time = null;

		if (text == null || time == null) {
			return text;
		}

		final Component newOutput = Component.empty()
			.append(LocalTime.ofInstant(time, ZoneId.systemDefault())
				.format(FireblanketConstants.COMMAND_EXECUTOR_FORMATTER))
			.append(text);

		this.lastOutput = newOutput;
		return newOutput;
	}

	@Unique
	private ParseResults<CommandSourceStack> fireblanket$fetchCachedResults(
		final Commands self,
		final CommandSourceStack source,
		final String command
	) {
		ParseResults<CommandSourceStack> parseResults = null;

		if (this.fireblanket$cache$parseResults != null) {
			parseResults = this.fireblanket$cache$parseResults.get();
		}

		if (parseResults == null) {
			return fireblanket$parseCommand(self, source, command);
		}

		// The following commented snippet would do it, but upon closer inspection,
		// it appears that Brigadier just self-mutates and gives a new ParseResult
		// that is otherwise exactly the same.
		// CommandManager.withCommandSource(parseResults, $ -> source);
		parseResults.getContext().withSource(source);

		return parseResults;
	}

	@Unique
	private ParseResults<CommandSourceStack> fireblanket$parseCommand(
		final Commands self,
		final CommandSourceStack source,
		final String command
	) {
		this.fireblanket$key$command = command;
		this.fireblanket$cache$command = Commands.trimOptionalPrefix(command);

		final CommandDispatcher<CommandSourceStack> dispatcher = self.getDispatcher();
		final ParseResults<CommandSourceStack> results = dispatcher.parse(
			this.fireblanket$cache$command,
			source
		);

		this.fireblanket$cache$parseResults = new SoftReference<>(results);

		return results;
	}

	/**
	 * @author Ampflower
	 * @reason Optimize message tracking. DATE_FORMAT takes a lot of CPU time.
	 */
	@Overwrite
	public void sendSystemMessage(final Component message) {
		if (!this.trackOutput) {
			return;
		}

		this.fireblanket$lastOutput$time = Instant.now();
		this.lastOutput = message;
		this.onUpdated();
	}

	@Shadow
	public void onUpdated() {
		throw new AssertionError();
	}
}
