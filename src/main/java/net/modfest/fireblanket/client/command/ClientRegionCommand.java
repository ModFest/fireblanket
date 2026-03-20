package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.client.DebugText;
import net.modfest.fireblanket.client.FireblanketDebug;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class ClientRegionCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("region")
			.then(literal("visualize")
				.executes(cl -> {
					Minecraft.getInstance().submit(() -> {
						if (Minecraft.getInstance().debugEntries.toggleStatus(FireblanketDebug.RENDER_REGION_VISUALIZE)) {
							cl.getSource().sendFeedback(DebugText.renderRegionVisualizeOn);
						} else {
							cl.getSource().sendFeedback(DebugText.renderRegionVisualizeOff);
						}
					});
					return 0;
				})
			)
			.then(literal("toggle")
				.executes(cl -> {
					if (ClientState.useRegionRenderer) {
						cl.getSource().sendFeedback(DebugText.renderRegionRenderingOff);
					} else {
						cl.getSource().sendFeedback(DebugText.renderRegionRenderingOn);
					}
					Minecraft.getInstance().submit(() -> ClientState.useRegionRenderer = !ClientState.useRegionRenderer);
					return 0;
				})
			)
		);
	}
}
