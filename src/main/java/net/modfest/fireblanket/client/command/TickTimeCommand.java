package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.modfest.fireblanket.client.ClientState;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class TickTimeCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("ticktimes")
			.executes(cl -> {
				if (!ClientState.displayTickTimes) {
					cl.getSource().sendFeedback(Text.literal("Displaying tick-times in world. WARNING: Tick times are influenced by a myriad of factors and require expert results to analyze!"));
					MinecraftClient.getInstance().submit(() -> ClientState.displayTickTimes = true);
				} else {
					MinecraftClient.getInstance().submit(() -> ClientState.displayTickTimes = false);
				}
				return 0;
			})
		);
	}
}
