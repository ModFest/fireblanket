package net.modfest.fireblanket.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixin.accessor.CommandBlockExecutorAccessor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.util.TextUtil;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntBiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class CmdFindReplaceCommand {

	private static final String[] sed = {
		"Replaced %s occurrence across %s block",
		"Replaced %s occurrences across %s block",
		"Replaced %s occurrences across %s blocks"
	};

	public static void init(LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext access) {
		base.then(literal("commandblock")
			.then(literal("sed")
				.requires(source -> source.hasPermission(4) && Roles.isNetadmin(source.getPlayer()))
				.then(argument("regex", StringArgumentType.string())
					.then(argument("replacement", StringArgumentType.string())
						.executes(ctx -> {
							Pattern p = Pattern.compile(StringArgumentType.getString(ctx, "regex"));
							String replacement = StringArgumentType.getString(ctx, "replacement");

							StringBuilder sb = new StringBuilder();

							final Counter count = iterate(
								ctx.getSource().getServer(),
								(text, cbe) -> replace(sb, p, replacement, cbe)
							);

							if (count.matches() == 0) {
								throw CommandUtils.GENERIC_EXCEPTION.create(Component.literal("No command blocks matched the regex"));
							} else {
								int resultIndex;
								if (count.blocks() == 1) {
									resultIndex = count.matches() > 1 ? 1 : 0;
								} else {
									resultIndex = 2;
								}

								ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
									"fireblanket.commands.command.sed.result." + resultIndex,
									sed[resultIndex],
									count.matches(),
									count.blocks()
								), true);
							}

							return 0;
						})
					)
				)
			)
		);

		// grep just needs org, not netadmin
		base.then(literal("commandblock")
			.then(literal("grep")
				.requires(source -> source.hasPermission(4) && Roles.isOrganizer(source.getPlayer()))
				.then(argument("regex", StringArgumentType.string())
					.executes(ctx -> {
						Pattern p = Pattern.compile(StringArgumentType.getString(ctx, "regex"));

						final MinecraftServer server = ctx.getSource().getServer();

						final Counter count = iterate(server, (text, cbe) -> {
							final Optional<Component> result = find(server, p, text, cbe);
							if (result.isEmpty()) {
								return 0;
							}
							ctx.getSource().sendSuccess(result::get, false);
							return 1;
						});

						ctx.getSource().sendSuccess(() -> Component.translatableWithFallback(
							"fireblanket.commands.command.grep.result",
							"Found %s matches.",
							count.matches()
						), false);

						return 0;
					})
				)
			)
		);
	}

	static Counter iterate(final MinecraftServer server, final ToIntBiFunction<Component, CommandBE> function) {
		int blocks = 0;
		int matches = 0;

		for (final ServerLevel world : server.getAllLevels()) {
			for (ChunkHolder holder : world.getChunkSource().chunkMap.getChunks()) {
				LevelChunk chunk = holder.getTickingChunk();
				if (chunk == null) {
					continue;
				}

				for (Map.Entry<BlockPos, BlockEntity> e : chunk.getBlockEntities().entrySet()) {
					if (e.getValue() instanceof CommandBE cbe) {
						final int count = function.applyAsInt(ExecutorUtils.toBlame(e.getValue()), cbe);
						if (count > 0) {
							blocks++;
							matches += count;
							e.getValue().setChanged();
						}
					}
				}
			}

			for (Entity entity : world.getAllEntities()) {
				if (entity instanceof CommandBE cbe) {
					final int count = function.applyAsInt(ExecutorUtils.toBlame(entity), cbe);
					if (count > 0) {
						blocks++;
						matches += count;
					}
				}
			}
		}

		return new Counter(blocks, matches);
	}

	private static int replace(final StringBuilder sb, final Pattern p, final String replacement, final CommandBE cbe) {
		int matches = 0;
		sb.setLength(0);
		BaseCommandBlock executor = cbe.fireblanket$getCommandExecutor();
		String cmd = executor.getCommand();
		Matcher m = p.matcher(cmd);
		while (m.find()) {
			matches++;
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		if (matches != 0) {
			executor.setCommand(sb.toString());
			executor.onUpdated();
		}
		return matches;
	}

	private static Optional<Component> find(
		final MinecraftServer server,
		final Pattern p,
		final Component name,
		final CommandBE cbe
	) {
		String cmd = cbe.fireblanket$getCommandExecutor().getCommand();
		Matcher m = p.matcher(cmd);

		if (!m.find()) {
			return Optional.empty();
		}

		String newCmd = m.replaceAll(result -> ChatFormatting.GOLD + result.group() + ChatFormatting.RESET);

		return toText(server, name, cbe, newCmd);
	}

	static Optional<Component> toText(
		final MinecraftServer server,
		final Component name,
		final CommandBE cbe,
		final String command
	) {
		UUID owner = cbe.fireblanket$getOwner();
		UUID lastUpdate = cbe.fireblanket$getLastUpdate();

		final BaseCommandBlock executor = cbe.fireblanket$getCommandExecutor();

		Component ownerName = TextUtil.getPlayerName(server, owner, "Unknown")
			.copy().withStyle(ChatFormatting.YELLOW);
		Component commandText = Component.literal(command).withStyle(ChatFormatting.GRAY)
			.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(executor.getLastOutput())));

		final Component lastExecuted;

		final long lastExecution = ((CommandBlockExecutorAccessor) executor).getLastExecution();

		if (lastExecution == -1) {
			lastExecuted = TextUtil.unknown.copy().withStyle(ChatFormatting.GRAY);
		} else {
			final long ticks = executor.getLevel().getGameTime() - lastExecution;

			final ChatFormatting formatting;
			if (ticks == 0) {
				formatting = ChatFormatting.RED;
			} else if (ticks <= 20) {
				formatting = ChatFormatting.GOLD;
			} else if (ticks <= 200) {
				formatting = ChatFormatting.YELLOW;
			} else {
				formatting = ChatFormatting.GREEN;
			}

			lastExecuted = ExecutorUtils.buildDuration(ticks)
				.withStyle(formatting);
		}

		if (Objects.equals(owner, lastUpdate)) {
			return Optional.of(Component.translatableWithFallback(
				"fireblanket.commands.command.grep.entry.same",
				"[%s] [%s] [%s]: %s",
				name,
				ownerName,
				lastExecuted,
				commandText
			));
		}

		Component lastUpdateName = TextUtil.getPlayerName(server, lastUpdate, "Unknown")
			.copy().withStyle(ChatFormatting.YELLOW);

		return Optional.of(Component.translatableWithFallback(
			"fireblanket.commands.command.grep.entry",
			"[%s] [%s / %s] [%s]: %s",
			name,
			ownerName,
			lastUpdateName,
			lastExecuted,
			commandText
		));
	}

	record Counter(int blocks, int matches) {
	}
}
