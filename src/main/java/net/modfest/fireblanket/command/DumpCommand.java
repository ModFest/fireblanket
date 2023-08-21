package net.modfest.fireblanket.command;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Map;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public class DumpCommand {
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("dump")
			.requires(source -> source.hasPermissionLevel(4))
			.then(literal("command-blocks")
				.executes(server -> {
					server.getSource().getServer().submit(() -> {
						ServerWorld world = server.getSource().getWorld();
						for (ChunkHolder holder : world.getChunkManager().threadedAnvilChunkStorage.entryIterator()) {
							WorldChunk chunk = holder.getWorldChunk();
							if (chunk == null) {
								continue;
							}
	
							for (Map.Entry<BlockPos, BlockEntity> e : chunk.getBlockEntities().entrySet()) {
								if (e.getValue() instanceof CommandBlockBlockEntity cbe) {
									BlockState state = cbe.getCachedState();

									String type;
									if (state.isOf(Blocks.COMMAND_BLOCK)) {
										type = "B";
									} else if (state.isOf(Blocks.CHAIN_COMMAND_BLOCK)) {
										type = "C";
									} else if (state.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
										type = "R";
									} else {
										type = "???";
									}

									String ft = type;
									server.getSource().sendFeedback(() -> Text.literal("[" + e.getKey().toShortString() + "] [" + ft + "] : " + cbe.getCommandExecutor().getCommand()), false);
								}
							}
						}
					});
	
					return 0;
				})
			)
			.then(literal("entity-types")
				.executes(server -> {
					for (EntityType<?> type : Registries.ENTITY_TYPE) {
						server.getSource().sendFeedback(() -> Text.literal(Registries.ENTITY_TYPE.getId(type) + " alwaysUpdateVelocity=" + type.alwaysUpdateVelocity() + " updateDistance(blocks)=" + (type.getMaxTrackDistance() * 16) + " tickInterval=" + type.getTrackTickInterval()), false);
					}
	
					return 0;
				})
			)
		);
	}
}
