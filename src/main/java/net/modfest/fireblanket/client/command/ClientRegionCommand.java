package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ClientRegionCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("region")
			.then(literal("visualize")
				.executes(cl -> {
					MinecraftClient.getInstance().submit(() -> RenderRegionRenderer.shouldRenderBox = !RenderRegionRenderer.shouldRenderBox);
					return 0;
				})
			)
			.then(literal("toggle")
				.executes(cl -> {
					if (RenderRegionRenderer.useRegionRenderer) {
						cl.getSource().sendFeedback(Text.literal("Disabling render region rendering."));
					} else {
						cl.getSource().sendFeedback(Text.literal("Enabling render region rendering."));
					}
					MinecraftClient.getInstance().submit(() -> RenderRegionRenderer.useRegionRenderer = !RenderRegionRenderer.useRegionRenderer);
					return 0;
				})
			)
		);
	}
}
