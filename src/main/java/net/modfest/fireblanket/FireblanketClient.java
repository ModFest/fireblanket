package net.modfest.fireblanket;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
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
                BlockEntityUpdateS2CPacket fakePacket = new BlockEntityUpdateS2CPacket(buf);
                client.execute(() -> handler.onBlockEntityUpdate(fakePacket));
            }
        });
    }
}
