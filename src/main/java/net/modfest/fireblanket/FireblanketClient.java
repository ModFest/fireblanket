package net.modfest.fireblanket;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.modfest.fireblanket.client.command.BERMaskCommand;
import net.modfest.fireblanket.mixin.ClientLoginNetworkHandlerAccessor;
import net.modfest.fireblanket.mixinsupport.FSCConnection;

public class FireblanketClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        if (FireblanketMixin.DO_BE_MASKING) {
            BERMaskCommand.init();
        }

        ClientLoginNetworking.registerGlobalReceiver(Fireblanket.FULL_STREAM_COMPRESSION, (client, handler, buf, listenerAdder) -> {
            if (Fireblanket.CAN_USE_ZSTD) {
                ((FSCConnection)((ClientLoginNetworkHandlerAccessor)handler).fireblanket$getConnection()).fireblanket$enableFullStreamCompression();
                return CompletableFuture.completedFuture(PacketByteBufs.empty());
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Fireblanket.BATCHED_BE_UPDATE, (client, handler, buf, sender) -> {
            int size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                BlockEntityUpdateS2CPacket fakePacket = new BlockEntityUpdateS2CPacket(buf);
                client.execute(() -> handler.onBlockEntityUpdate(fakePacket));
            }
        });
    }
}
