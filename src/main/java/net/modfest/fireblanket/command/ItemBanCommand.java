package net.modfest.fireblanket.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.world.ItemBan;

import static net.minecraft.commands.Commands.LEVEL_OWNERS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ItemBanCommand {
	public static void init(LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext access) {
		base.then(literal("itemban")
			.requires(source -> LEVEL_OWNERS.check(source.permissions()) || Roles.isOrganizer(source.getPlayer()))
			.then(literal("list").executes(s -> execList(s.getSource())))
			.then(literal("add")
				.then(argument("value", StringArgumentType.greedyString())
					// Delegate suggestions to the registry argument type
					.suggests(ResourceKeyArgument.key(Registries.ITEM)::listSuggestions)
					.executes(s -> execAdd(s.getSource(), StringArgumentType.getString(s, "value")))))
			.then(literal("remove")
				.then(
					argument("value", StringArgumentType.greedyString())
						.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(ItemBan.BANNED_IDS, builder))
						.executes(s -> execRemove(s.getSource(), StringArgumentType.getString(s, "value")))
				)
			)
		);
	}

	private static int execList(CommandSourceStack src) {
		src.sendSuccess(() -> Component.literal("Currently banned items: " + ItemBan.BANNED_IDS), false);
		return 0;
	}

	private static int execAdd(CommandSourceStack src, String value) {
		if (ItemBan.BANNED_IDS.add(value)) {
			src.sendSuccess(() -> Component.literal("Successfully added " + value + "."), false);
		} else {
			src.sendFailure(Component.literal("Didn't add as it is already banned"));
		}
		return 0;
	}

	private static int execRemove(CommandSourceStack src, String value) {
		if (ItemBan.BANNED_IDS.remove(value)) {
			src.sendSuccess(() -> Component.literal("Successfully removed " + value + "."), false);
		} else {
			src.sendFailure(Component.literal("Didn't remove as it wasn't banned"));
		}
		return 0;
	}
}
