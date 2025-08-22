package net.modfest.fireblanket.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.world.ItemBan;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ItemBanCommand {
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("itemban")
			.requires(source -> source.hasPermissionLevel(4) || Roles.isOrganizer(source.getPlayer()))
			.then(literal("list").executes(s -> execList(s.getSource())))
			.then(literal("add")
				.then(argument("value", StringArgumentType.greedyString())
					// Delegate suggestions to the registry argument type
					.suggests(RegistryKeyArgumentType.registryKey(RegistryKeys.ITEM)::listSuggestions)
					.executes(s -> execAdd(s.getSource(), StringArgumentType.getString(s, "value")))))
			.then(literal("remove")
				.then(
					argument("value", StringArgumentType.greedyString())
						.suggests((ctx, builder) -> CommandSource.suggestMatching(ItemBan.BANNED_IDS, builder))
						.executes(s -> execRemove(s.getSource(), StringArgumentType.getString(s, "value")))
				)
			)
		);
	}

	private static int execList(ServerCommandSource src) {
		src.sendFeedback(() -> Text.literal("Currently banned items: " + ItemBan.BANNED_IDS), false);
		return 0;
	}

	private static int execAdd(ServerCommandSource src, String value) {
		if (ItemBan.BANNED_IDS.add(value)) {
			src.sendFeedback(() -> Text.literal("Successfully added " + value + "."), false);
		} else {
			src.sendError(Text.literal("Didn't add as it is already banned"));
		}
		return 0;
	}

	private static int execRemove(ServerCommandSource src, String value) {
		if (ItemBan.BANNED_IDS.remove(value)) {
			src.sendFeedback(() -> Text.literal("Successfully removed " + value + "."), false);
		} else {
			src.sendError(Text.literal("Didn't remove as it wasn't banned"));
		}
		return 0;
	}
}
