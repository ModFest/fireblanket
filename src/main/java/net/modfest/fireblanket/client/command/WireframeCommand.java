package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class WireframeCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("wireframe")
			.requires(_ -> SharedConstants.DEBUG_HOTKEYS)
			.executes(_ -> {
				Minecraft.getInstance().submit(() -> {
					Minecraft minecraft = Minecraft.getInstance();
					minecraft.wireframe = !minecraft.wireframe;
				});
				return 0;
			})
		);
	}
}
