package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.modfest.fireblanket.client.ClientState;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class TickTimeCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("ticktimes")
			.executes(cl -> {
				if (!ClientState.displayTickTimes) {
					cl.getSource().sendFeedback(Component.literal("Displaying tick-times in world. WARNING: Tick times are influenced by a myriad of factors and require expert knowledge to analyze!"));
					Minecraft.getInstance().submit(() -> ClientState.displayTickTimes = true);
				} else {
					Minecraft.getInstance().submit(() -> ClientState.displayTickTimes = false);
				}
				return 0;
			})
		);
	}
}
