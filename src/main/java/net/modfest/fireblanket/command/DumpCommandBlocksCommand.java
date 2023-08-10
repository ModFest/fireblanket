package net.modfest.fireblanket.command;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Map;

public class DumpCommandBlocksCommand {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((event, access, env) -> {
            event.register(CommandManager.literal("fireblanket:dumpcommandblocks")
                            .requires(source -> source.hasPermissionLevel(4))
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
                                        server.getSource().sendFeedback(() -> Text.literal("[" + e.getKey().toShortString() + "]: " + cbe.getCommandExecutor().getCommand()), false);
                                    }
                                }
                            }
                        });

                        return 0;
                    })
            );
        });
    }
}
