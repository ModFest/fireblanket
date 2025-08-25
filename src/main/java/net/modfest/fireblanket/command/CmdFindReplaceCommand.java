package net.modfest.fireblanket.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.chunk.WorldChunk;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.util.TextUtil;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToIntBiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class CmdFindReplaceCommand {

	private static final String[] sed = {
		"Replaced %s occurrence across %s block",
		"Replaced %s occurrences across %s block",
		"Replaced %s occurrences across %s blocks"
	};

	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("commandblock")
			.then(literal("sed")
				.requires(source -> source.hasPermissionLevel(4) && Roles.isNetadmin(source.getPlayer()))
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
								throw CommandUtils.GENERIC_EXCEPTION.create(Text.literal("No command blocks matched the regex"));
							} else {
								int resultIndex;
								if (count.blocks() == 1) {
									resultIndex = count.matches() > 1 ? 1 : 0;
								} else {
									resultIndex = 2;
								}

								ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(
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
				.requires(source -> source.hasPermissionLevel(4) && Roles.isOrganizer(source.getPlayer()))
				.then(argument("regex", StringArgumentType.string())
					.executes(ctx -> {
						Pattern p = Pattern.compile(StringArgumentType.getString(ctx, "regex"));

						final MinecraftServer server = ctx.getSource().getServer();

						final Counter count = iterate(server, (text, cbe) -> {
							final Optional<Text> result = find(server, p, text, cbe);
							if (result.isEmpty()) {
								return 0;
							}
							ctx.getSource().sendFeedback(result::get, false);
							return 1;
						});

						ctx.getSource().sendFeedback(() -> Text.translatableWithFallback(
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

	private static Counter iterate(final MinecraftServer server, final ToIntBiFunction<Text, CommandBE> function) {
		int blocks = 0;
		int matches = 0;

		for (final ServerWorld world : server.getWorlds()) {
			for (ChunkHolder holder : world.getChunkManager().chunkLoadingManager.entryIterator()) {
				WorldChunk chunk = holder.getWorldChunk();
				if (chunk == null) {
					continue;
				}

				for (Map.Entry<BlockPos, BlockEntity> e : chunk.getBlockEntities().entrySet()) {
					if (e.getValue() instanceof CommandBE cbe) {
						final int count = function.applyAsInt(TextUtil.ofLocationWithTeleport(world, e.getKey()), cbe);
						if (count > 0) {
							blocks++;
							matches += count;
							e.getValue().markDirty();
						}
					}
				}
			}

			for (Entity entity : world.iterateEntities()) {
				if (entity instanceof CommandBE cbe) {
					final int count = function.applyAsInt(TextUtil.ofEntityWithTeleport(entity), cbe);
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
		CommandBlockExecutor executor = cbe.fireblanket$getCommandExecutor();
		String cmd = executor.getCommand();
		Matcher m = p.matcher(cmd);
		while (m.find()) {
			matches++;
			m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);
		if (matches != 0) {
			executor.setCommand(sb.toString());
			executor.markDirty();
		}
		return matches;
	}

	private static Optional<Text> find(
		final MinecraftServer server,
		final Pattern p,
		final Text name,
		final CommandBE cbe
	) {
		String cmd = cbe.fireblanket$getCommandExecutor().getCommand();
		Matcher m = p.matcher(cmd);

		if (!m.find()) {
			return Optional.empty();
		}

		String newCmd = m.replaceAll(result -> Formatting.GOLD + result.group() + Formatting.RESET);

		UUID owner = cbe.fireblanket$getOwner();
		UUID lastUpdate = cbe.fireblanket$getLastUpdate();

		Text ownerName = TextUtil.getPlayerName(server, owner, "Unknown owner!!");
		Text lastUpdateName = TextUtil.getPlayerName(server, lastUpdate, "Unknown last update!!");

		return Optional.of(Text.translatableWithFallback(
			"fireblanket.commands.command.grep.entry",
			"[%s] [Owner: %s] [Last updated: %s]: %s",
			name,
			ownerName,
			lastUpdateName,
			newCmd
		));
	}

	private record Counter(int blocks, int matches) {
	}
}
