package net.modfest.fireblanket.net.writer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;


/**
 * ServerPacketWriter is a utility method for creating CustomPayloadS2CPackets that write on network thread
 * instead of server one.
 *
 * This is a (minimally) simplified version of interface from Polymer Networking API.
 * In case of bigger data (like polymer's registry sync), performance increase was noticeable,
 * so I thought it would be good idea to bring it here.
 *
 * Vanilla adopted similar (through bit fancier) approach with it's own CustomPayload packets in 1.20.2 (23w31a)
 */
public interface ServerPacketWriter {
    void write(PacketByteBuf buf, Identifier packetId);

    default Packet<ClientPlayPacketListener> toPacket(Identifier identifier) {
        var base = new CustomPayloadS2CPacket(identifier, new PacketByteBuf(Unpooled.EMPTY_BUFFER));
        ((CustomPayloadS2CExt) base).fireblanket$setWriter(this);
        return base;
    }
}
