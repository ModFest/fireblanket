package net.modfest.fireblanket.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SpawnCommand {
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.executes(ctx -> {
			ServerPlayerEntity player = ctx.getSource().getPlayer();
			ServerWorld overworld = ctx.getSource().getServer().getOverworld();
			if(player == null || overworld == null) return 0;
			
			BlockPos s = overworld.getSpawnPos();
			player.teleport(overworld, s.getX() + 0.5, s.getY(), s.getZ() + 0.5, overworld.getSpawnAngle(), 0);
			ctx.getSource().sendFeedback(() -> Text.literal("\u00a7aWhoosh!"), false);
			
			return 1;
		});
	}
}
