package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.modfest.fireblanket.client.render.RenderRegionRenderer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class ClientRegionCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("region")
			.then(literal("visualize")
				.executes(cl -> {
					Minecraft.getInstance().submit(() -> RenderRegionRenderer.shouldRenderBox = !RenderRegionRenderer.shouldRenderBox);
					return 0;
				})
			)
			.then(literal("toggle")
				.executes(cl -> {
					if (RenderRegionRenderer.useRegionRenderer) {
						cl.getSource().sendFeedback(Component.literal("Disabling render region rendering."));
					} else {
						cl.getSource().sendFeedback(Component.literal("Enabling render region rendering."));
					}
					Minecraft.getInstance().submit(() -> RenderRegionRenderer.useRegionRenderer = !RenderRegionRenderer.useRegionRenderer);
					return 0;
				})
			)
		);
	}
}
