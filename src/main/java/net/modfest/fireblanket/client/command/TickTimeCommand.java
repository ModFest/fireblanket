package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.modfest.fireblanket.client.FireblanketDebug;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class TickTimeCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("ticktimes")
			.executes(cl -> {
				Minecraft.getInstance().submit(() -> FireblanketDebug.toggleTickTimes(cl.getSource()::sendFeedback));
				return 0;
			})
		);
	}
}
