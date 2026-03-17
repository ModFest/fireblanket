package net.modfest.fireblanket.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.modfest.fireblanket.compat.roles.Roles;
import net.modfest.fireblanket.util.TextUtil;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class StareCommand {
	public static void init(LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext access) {
		base.then(literal("whoareyou")
			.requires(source -> source.hasPermission(4) || Roles.isOrganizer(source.getPlayer()))
			.then(argument("targets", EntityArgument.entities())
				.executes(ctx -> {
					for (Entity t : EntityArgument.getEntities(ctx, "targets")) {
						ctx.getSource().sendSuccess(() -> Component.literal(t.getStringUUID()), false);
					}
					return 1;
				})
			));

		// Harmless debug command. May come in handy for someone, someday.
		base.then(literal("whoami")
			.requires(source -> source.hasPermission(2) || Roles.isOrganizer(source.getPlayer()))
			.executes(ctx -> {
				final CommandSourceStack source = ctx.getSource();
				source.sendSuccess(() -> TextUtil.ofRunner(source), false);
				return 1;
			})
		);
	}
}
