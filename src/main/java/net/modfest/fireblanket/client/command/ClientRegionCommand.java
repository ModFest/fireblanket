package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientRegionCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("region")
			.then(literal("visualize")
				.executes(cl -> {
					MinecraftClient.getInstance().submit(() -> RenderRegionRenderer.shouldRender = !RenderRegionRenderer.shouldRender);
					return 0;
				})
			)
		);
	}
}
