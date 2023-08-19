package net.modfest.fireblanket.net;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.net.writer.ServerPacketWriter;


public record BundledBlockEntityUpdatePacket(BEUpdate[] updates) implements ServerPacketWriter {

	@Override
	public void write(PacketByteBuf buf, Identifier packetId) {
		int size = updates.length;
		buf.writeVarInt(size);
		for (int i = 0; i < size; i++) {
			BEUpdate bup = updates[i];
			buf.writeBlockPos(bup.pos());
			buf.writeRegistryValue(Registries.BLOCK_ENTITY_TYPE, bup.type());
			buf.writeNbt(bup.nbt());
		}
	}
}
