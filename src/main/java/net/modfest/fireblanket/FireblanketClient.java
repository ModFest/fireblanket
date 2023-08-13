package net.modfest.fireblanket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.client.command.BERMaskCommand;

public class FireblanketClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FireblanketMixin.DO_BE_MASKING) {
            BERMaskCommand.init();
        }

        ClientPlayNetworking.registerGlobalReceiver(Fireblanket.BATCHED_BE_UPDATE, (client, handler, buf, sender) -> {
            int size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                BlockPos pos = buf.readBlockPos();
                BlockEntityType<?> type = buf.readRegistryValue(Registries.BLOCK_ENTITY_TYPE);
                NbtCompound nbt = buf.readNbt();

                client.execute(() -> {
                    BlockEntity be = client.world.getBlockEntity(pos);
                    if (be == null || be.getType() != type) {
                        return;
                    }

                    if (nbt != null) {
                        be.readNbt(nbt);
                    }

                    if (be instanceof CommandBlockBlockEntity && client.currentScreen instanceof CommandBlockScreen) {
                        ((CommandBlockScreen) client.currentScreen).updateCommandBlock();
                    }
                });
            }
        });
    }
}
