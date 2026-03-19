package net.modfest.fireblanket.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseCommandBlock;
import net.modfest.fireblanket.compat.roles.Roles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static net.minecraft.commands.Commands.LEVEL_OWNERS;
import static net.minecraft.commands.Commands.literal;

public final class DumpCommand {
	public static void init(LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext access) {
		base.then(literal("dump")
			.requires(source -> LEVEL_OWNERS.check(source.permissions()) || Roles.isOrganizer(source.getPlayer()))
			.then(literal("command-blocks")
				.executes(ctx -> {
					final MinecraftServer server = ctx.getSource().getServer();

					final CmdFindReplaceCommand.Counter counter = CmdFindReplaceCommand.iterate(server, (text, cbe, level) -> {
						final BaseCommandBlock executor = cbe.fireblanket$getCommandExecutor();

						final Optional<Component> result = CmdFindReplaceCommand.toText(server, level, text, cbe, executor.getCommand());

							if (result.isEmpty()) {
								return 0;
							}
						ctx.getSource().sendSuccess(result::get, false);
							return 1;
						}
					);


					return counter.blocks();
				})
			)
			.then(literal("entity-types")
				.executes(server -> {
					for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
						server.getSource().sendSuccess(() -> Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(type) + " alwaysUpdateVelocity=" + type.trackDeltas() + " updateDistance(blocks)=" + (type.clientTrackingRange() * 16) + " tickInterval=" + type.updateInterval()), false);
					}

					return 0;
				})
			)
			.then(literal("entities")
				.executes(cmd -> {
					MinecraftServer server = cmd.getSource().getServer();
					server.submit(() -> {
						for (ServerLevel world : server.getAllLevels()) {
							cmd.getSource().sendSuccess(() -> Component.literal("----- Dumping types for dimension " + world.dimensionTypeRegistration().unwrapKey().get().identifier() + " --------"), false);

							Object2IntOpenHashMap<EntityType<?>> map = new Object2IntOpenHashMap<>();

							for (Entity entity : world.getAllEntities()) {
								int r = map.getOrDefault(entity.getType(), 0);
								map.put(entity.getType(), r + 1);
							}

							ArrayList<Object2IntMap.Entry<EntityType<?>>> entries = new ArrayList<>(map.object2IntEntrySet());
							entries.sort(Map.Entry.comparingByValue());
							Collections.reverse(entries);
							for (Object2IntMap.Entry<EntityType<?>> e : entries) {
								EntityType<?> key = e.getKey();
								String string = BuiltInRegistries.ENTITY_TYPE.getKey(key).toString();

								cmd.getSource().sendSuccess(() -> Component.literal(string + " -> " + e.getIntValue()), false);
							}
						}
					});

					return 0;
				})
			)
		);
	}
}
