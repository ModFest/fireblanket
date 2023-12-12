package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.modfest.fireblanket.client.ClientState;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class WireframeCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("wireframe")
			.executes(cl -> {
				MinecraftClient.getInstance().submit(() -> ClientState.wireframe = !ClientState.wireframe);
				return 0;
			})
		);
	}
}
