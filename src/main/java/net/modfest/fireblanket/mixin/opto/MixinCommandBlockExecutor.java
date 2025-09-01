package net.modfest.fireblanket.mixin.opto;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.world.CommandBlockExecutor;
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
@Mixin(value = CommandBlockExecutor.class, priority = 1100)
public class MixinCommandBlockExecutor {
	@Unique
	private String fireblanket$key$command;
	@Unique
	private String fireblanket$cache$command;
	@Unique
	private SoftReference<ParseResults<ServerCommandSource>> fireblanket$cache$parseResults;

	@Unique
	private Instant fireblanket$lastOutput$time;

	@Shadow
	private Text lastOutput;
	@Shadow
	private boolean trackOutput;
	@Shadow
	private String command;

	@Redirect(
		method = "execute",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/CommandManager;executeWithPrefix(Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)V")
	)
	private void fireblanket$executeCached(
		final CommandManager self,
		final ServerCommandSource source,
		final String command
	) {
		final ParseResults<ServerCommandSource> parseResults = fireblanket$fetchCachedResults(self, source, command);

		self.execute(parseResults, this.fireblanket$cache$command);
	}

	@Inject(
		method = "*",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/CommandBlockExecutor;command:Ljava/lang/String;",
			opcode = Opcodes.PUTFIELD,
			shift = At.Shift.AFTER
		)
	)
	private void fireblanket$onSetCommand(final CallbackInfo ci) {
		if (StringHelper.isEmpty(this.command) || !this.command.equals(this.fireblanket$key$command)) {
			this.fireblanket$cache$parseResults = null;
		}
		this.fireblanket$key$command = this.command;
	}

	/**
	 * @author Ampflower
	 * @reason Avoid needlessly calling {@link #fireblanket$lazy$lastOutput(Text)} twice.
	 */
	@Overwrite
	public Text getLastOutput() {
		final Text text = this.lastOutput;
		if (text == null) {
			return ScreenTexts.EMPTY;
		}
		return fireblanket$lazy$lastOutput(text);
	}

	@ModifyExpressionValue(
		method = "*",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/CommandBlockExecutor;lastOutput:Lnet/minecraft/text/Text;",
			opcode = Opcodes.GETFIELD
		)
	)
	private Text fireblanket$lazy$lastOutput(final Text text) {
		final Instant time = this.fireblanket$lastOutput$time;
		this.fireblanket$lastOutput$time = null;

		if (text == null || time == null) {
			return text;
		}

		final Text newOutput = Text.empty()
			.append(LocalTime.ofInstant(time, ZoneId.systemDefault())
				.format(FireblanketConstants.COMMAND_EXECUTOR_FORMATTER))
			.append(text);

		this.lastOutput = newOutput;
		return newOutput;
	}

	@Unique
	private ParseResults<ServerCommandSource> fireblanket$fetchCachedResults(
		final CommandManager self,
		final ServerCommandSource source,
		final String command
	) {
		ParseResults<ServerCommandSource> parseResults = null;

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
	private ParseResults<ServerCommandSource> fireblanket$parseCommand(
		final CommandManager self,
		final ServerCommandSource source,
		final String command
	) {
		this.fireblanket$key$command = command;
		this.fireblanket$cache$command = CommandManager.stripLeadingSlash(command);

		final CommandDispatcher<ServerCommandSource> dispatcher = self.getDispatcher();
		final ParseResults<ServerCommandSource> results = dispatcher.parse(
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
	public void sendMessage(final Text message) {
		if (!this.trackOutput) {
			return;
		}

		this.fireblanket$lastOutput$time = Instant.now();
		this.lastOutput = message;
		this.markDirty();
	}

	@Shadow
	public void markDirty() {
		throw new AssertionError();
	}
}
