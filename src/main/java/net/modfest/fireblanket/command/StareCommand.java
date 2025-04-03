package net.modfest.fireblanket.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.modfest.fireblanket.compat.roles.Roles;

import static net.minecraft.server.command.CommandManager.*;

public class StareCommand {
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("whoareyou")
			.requires(source -> source.hasPermissionLevel(4) || Roles.isOrganizer(source.getPlayer()))
			.then(argument("targets", EntityArgumentType.entities())
				.executes(ctx -> {
					for (Entity t : EntityArgumentType.getEntities(ctx, "targets")) {
						ctx.getSource().sendFeedback(() -> Text.literal(t.getUuidAsString()), false);
					}
					return 1;
				})
			));
	}
}
