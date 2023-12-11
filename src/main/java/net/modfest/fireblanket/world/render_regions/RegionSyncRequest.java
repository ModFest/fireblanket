package net.modfest.fireblanket.world.render_regions;

import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.world.render_regions.RenderRegion.Mode;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.*;

public sealed interface RegionSyncRequest permits InvalidCommand, FullState, Reset, AddRegion,
		DestroyRegion, DetachAll, AttachEntity, AttachBlock, DetachEntity, DetachBlock, RedefineRegion, FullStateLegacy,
		RegistryRegionSyncRequest {

	public enum Type {
		INVALID_COMMAND(InvalidCommand::read),
		FULL_STATE_LEGACY(FullStateLegacy::read),
		RESET(Reset::read),
		ADD_REGION(AddRegion::read),
		DESTROY_REGION(DestroyRegion::read),
		DETACH_ALL(DetachAll::read),
		ATTACH_ENTITY(AttachEntity::read),
		ATTACH_BLOCK(AttachBlock::read),
		DETACH_ENTITY(DetachEntity::read),
		DETACH_BLOCK(DetachBlock::read),
		REDEFINE_REGION(RedefineRegion::read),
		ATTACH_ENTITY_TYPE(AttachEntityType::read),
		DETACH_ENTITY_TYPE(DetachEntityType::read),
		ATTACH_BLOCK_ENTITY_TYPE(AttachBlockEntityType::read),
		DETACH_BLOCK_ENTITY_TYPE(DetachBlockEntityType::read),
		FULL_STATE(FullState::read),
		;
		public static final ImmutableList<Type> VALUES = ImmutableList.copyOf(values());
		public final Function<PacketByteBuf, ? extends RegionSyncRequest> reader;

		Type(Function<PacketByteBuf, ? extends RegionSyncRequest> reader) {
			this.reader = reader;
		}
		
	}
	
	Type type();
	
//  static self read(PacketByteBuf buf);
	void write(PacketByteBuf buf);
	
	void apply(RenderRegions tgt);
	
	boolean valid();
	
	sealed interface RegistryRegionSyncRequest<T> extends RegionSyncRequest permits AttachEntityType, DetachEntityType,
			AttachBlockEntityType, DetachBlockEntityType {
		
		Registry<T> registry();
		
		String name();
		Identifier id();

		@Override
		default boolean valid() {
			return name() != null && registry().get(id()) != null;
		}

		@Override
		default void write(PacketByteBuf buf) {
			buf.writeString(name());
			writeId(buf, registry(), id());
		}
	}

	default void write(PacketByteBuf buf, Identifier packetId) {
		buf.writeByte(type().ordinal());
		write(buf);
	}

	default Packet<ClientCommonPacketListener> toPacket(Identifier id) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		write(buf, id);

		return ServerPlayNetworking.createS2CPacket(id, buf);
	}


	private static void writeRegion(PacketByteBuf buf, RenderRegion r) {
		buf.writeByte(r.mode().ordinal());
		buf.writeVarInt(r.minX()).writeVarInt(r.minY()).writeVarInt(r.minZ());
		buf.writeVarInt(r.maxX()).writeVarInt(r.maxY()).writeVarInt(r.maxZ());
	}
	
	private static RenderRegion readRegion(PacketByteBuf buf) {
		int modeId = buf.readUnsignedByte();
		if (modeId >= Mode.VALUES.size()) {
			Fireblanket.LOGGER.warn("Unknown region mode id "+modeId);
			modeId = 0;
		}
		Mode mode = Mode.VALUES.get(modeId);
		return new RenderRegion(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
				buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
				mode);
	}
	
	private static <T> Identifier readId(PacketByteBuf buf, Registry<T> registry) {
		return registry.getId(registry.get(buf.readVarInt()));
	}
	
	private static <T> void writeId(PacketByteBuf buf, Registry<T> registry, Identifier id) {
		buf.writeVarInt(registry.getRawId(registry.get(id)));
	}
	
	static RegionSyncRequest read(PacketByteBuf buf) {
		int tid = buf.readUnsignedByte();
		if (tid >= Type.VALUES.size()) {
			Fireblanket.LOGGER.warn("Unknown region sync command id "+tid);
			return new InvalidCommand();
		}
		Type t = Type.VALUES.get(tid);
		return t.reader.apply(buf);
	}
	
	public record InvalidCommand() implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.INVALID_COMMAND;
		}

		@Override
		public void write(PacketByteBuf buf) {
			Fireblanket.LOGGER.warn("Writing an invalid command");
		}

		public static InvalidCommand read(PacketByteBuf buf) {
			return new InvalidCommand();
		}
		
		@Override
		public void apply(RenderRegions tgt) {
			Fireblanket.LOGGER.warn("Attempted to apply invalid command");
		}

		@Override
		public boolean valid() {
			return false;
		}
		
	}
	
	public record FullStateLegacy(ImmutableMap<String, RenderRegion> regions,
			ImmutableMultimap<RenderRegion, UUID> entityAttachments,
			ImmutableMultimap<RenderRegion, Long> blockAttachments) implements RegionSyncRequest {
		
		@Override
		public Type type() {
			return Type.FULL_STATE_LEGACY;
		}

		@Override
		public void write(PacketByteBuf buf) {
			throw new UnsupportedOperationException();
		}
		
		public static FullStateLegacy read(PacketByteBuf buf) {
			ImmutableMap.Builder<String, RenderRegion> regionsBldr = ImmutableMap.builder();
			ImmutableMultimap.Builder<RenderRegion, UUID> entityAttachmentsBldr = ImmutableMultimap.builder();
			ImmutableMultimap.Builder<RenderRegion, Long> blockAttachmentsBldr = ImmutableMultimap.builder();
			int regionCount = buf.readVarInt();
			for (int i = 0; i < regionCount; i++) {
				String name = buf.readString();
				RenderRegion r = readRegion(buf);
				regionsBldr.put(name, r);
				int entityCount = buf.readVarInt();
				for (int j = 0; j < entityCount; j++) {
					entityAttachmentsBldr.put(r, buf.readUuid());
				}
				int blockCount = buf.readVarInt();
				for (int j = 0; j < blockCount; j++) {
					blockAttachmentsBldr.put(r, buf.readLong());
				}
			}
			return new FullStateLegacy(regionsBldr.build(), entityAttachmentsBldr.build(), blockAttachmentsBldr.build());
		}
		
		@Override
		public boolean valid() {
			return regions != null && entityAttachments != null && blockAttachments != null
					&& !regions.isEmpty();
		}
		
		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
			regions.forEach(tgt::add);
			entityAttachments.forEach(tgt::attachEntity);
			blockAttachments.forEach(tgt::attachBlock);
		}
	}
	
	public record Reset(boolean valid) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.RESET;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeInt(0xDEADDEAD);
		}
		
		public static Reset read(PacketByteBuf buf) {
			return new Reset(buf.readInt() == 0xDEADDEAD);
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
		}
		
	}
	
	public record AddRegion(String name, RenderRegion region) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.ADD_REGION;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			writeRegion(buf, region);
		}
		
		public static AddRegion read(PacketByteBuf buf) {
			return new AddRegion(buf.readString(), readRegion(buf));
		}
		
		@Override
		public boolean valid() {
			return name != null && region != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.add(name, region);
		}
		
	}
	
	public record DestroyRegion(String name) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.DESTROY_REGION;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
		}
		
		public static DestroyRegion read(PacketByteBuf buf) {
			return new DestroyRegion(buf.readString());
		}
		
		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.remove(tgt.getByName(name));
		}
		
	}
	
	public record DetachAll(String name) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.DETACH_ALL;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
		}
		
		public static DetachAll read(PacketByteBuf buf) {
			return new DetachAll(buf.readString());
		}
		
		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachAll(tgt.getByName(name));
		}
		
	}
	
	public record AttachEntity(String name, UUID entity) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.ATTACH_ENTITY;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeUuid(entity);
		}
		
		public static AttachEntity read(PacketByteBuf buf) {
			return new AttachEntity(buf.readString(), buf.readUuid());
		}
		
		@Override
		public boolean valid() {
			return name != null && entity != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachEntity(tgt.getByName(name), entity);
		}
		
	}
	
	public record AttachBlock(String name, long pos) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.ATTACH_BLOCK;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeLong(pos);
		}
		
		public static AttachBlock read(PacketByteBuf buf) {
			return new AttachBlock(buf.readString(), buf.readLong());
		}

		@Override
		public boolean valid() {
			return name != null;
		}
		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachBlock(tgt.getByName(name), pos);
		}
		
	}
	
	public record DetachEntity(String name, UUID entity) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.DETACH_ENTITY;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeUuid(entity);
		}
		
		public static DetachEntity read(PacketByteBuf buf) {
			return new DetachEntity(buf.readString(), buf.readUuid());
		}
		
		@Override
		public boolean valid() {
			return name != null && entity != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachEntity(tgt.getByName(name), entity);
		}
		
	}
	
	public record DetachBlock(String name, long pos) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.DETACH_BLOCK;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			buf.writeLong(pos);
		}
		
		public static DetachBlock read(PacketByteBuf buf) {
			return new DetachBlock(buf.readString(), buf.readLong());
		}
		
		@Override
		public boolean valid() {
			return name != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachBlock(tgt.getByName(name), pos);
		}
		
	}
	
	public record RedefineRegion(String name, RenderRegion region) implements RegionSyncRequest {

		@Override
		public Type type() {
			return Type.REDEFINE_REGION;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(name);
			writeRegion(buf, region);
		}
		
		public static RedefineRegion read(PacketByteBuf buf) {
			return new RedefineRegion(buf.readString(), readRegion(buf));
		}
		
		@Override
		public boolean valid() {
			return name != null && region != null;
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.redefine(name, region);
		}
		
	}
	
	public record AttachEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<EntityType<?>> {

		@Override
		public Type type() {
			return Type.ATTACH_ENTITY_TYPE;
		}
		
		@Override
		public Registry<EntityType<?>> registry() {
			return Registries.ENTITY_TYPE;
		}
		
		public static AttachEntityType read(PacketByteBuf buf) {
			return new AttachEntityType(buf.readString(), readId(buf, Registries.ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachEntityType(tgt.getByName(name), id);
		}
		
	}
	
	public record DetachEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<EntityType<?>> {

		@Override
		public Type type() {
			return Type.DETACH_ENTITY_TYPE;
		}
		
		@Override
		public Registry<EntityType<?>> registry() {
			return Registries.ENTITY_TYPE;
		}
		
		public static DetachEntityType read(PacketByteBuf buf) {
			return new DetachEntityType(buf.readString(), readId(buf, Registries.ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachEntityType(tgt.getByName(name), id);
		}
		
	}
	
	public record AttachBlockEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<BlockEntityType<?>> {

		@Override
		public Type type() {
			return Type.ATTACH_BLOCK_ENTITY_TYPE;
		}
		
		@Override
		public Registry<BlockEntityType<?>> registry() {
			return Registries.BLOCK_ENTITY_TYPE;
		}
		
		public static AttachBlockEntityType read(PacketByteBuf buf) {
			return new AttachBlockEntityType(buf.readString(), readId(buf, Registries.BLOCK_ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachBlockEntityType(tgt.getByName(name), id);
		}
		
	}
	
	public record DetachBlockEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<BlockEntityType<?>> {

		@Override
		public Type type() {
			return Type.DETACH_BLOCK_ENTITY_TYPE;
		}
		
		@Override
		public Registry<BlockEntityType<?>> registry() {
			return Registries.BLOCK_ENTITY_TYPE;
		}
		
		public static DetachBlockEntityType read(PacketByteBuf buf) {
			return new DetachBlockEntityType(buf.readString(), readId(buf, Registries.BLOCK_ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachBlockEntityType(tgt.getByName(name), id);
		}
		
	}
	
	public record FullState(ImmutableList<ExplainedRenderRegion> regions) implements RegionSyncRequest {
		
		@Override
		public Type type() {
			return Type.FULL_STATE;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(regions.size());
			for (var ex : regions) {
				buf.writeString(ex.name);
				int sizePos = buf.writerIndex();
				buf.writeMedium(0);
				int start = buf.writerIndex();
				RenderRegion r = ex.reg;
				writeRegion(buf, r);
				var ea = ex.entityAttachments;
				buf.writeVarInt(ea.size());
				for (UUID id : ea) {
					buf.writeUuid(id);
				}
				var ba = ex.blockAttachments;
				buf.writeVarInt(ba.size());
				LongIterator iter = ba.longIterator();
				while (iter.hasNext()) {
					buf.writeLong(iter.nextLong());
				}
				var et = ex.entityTypeAttachments;
				buf.writeVarInt(et.size());
				for (Identifier id : et) {
					writeId(buf, Registries.ENTITY_TYPE, id);
				}
				var bet = ex.beTypeAttachments;
				buf.writeVarInt(bet.size());
				for (Identifier id : bet) {
					writeId(buf, Registries.BLOCK_ENTITY_TYPE, id);
				}
				int len = buf.writerIndex()-start;
				buf.markWriterIndex();
				buf.writerIndex(sizePos);
				buf.writeMedium(len);
				buf.resetWriterIndex();
			}
		}
		
		public static FullState read(PacketByteBuf buf) {
			ImmutableList.Builder<ExplainedRenderRegion> bldr = ImmutableList.builder();
			int regionCount = buf.readVarInt();
			for (int i = 0; i < regionCount; i++) {
				String name = buf.readString();
				int len = buf.readUnsignedMedium();
				int start = buf.readerIndex();
				RenderRegion r = readRegion(buf);
				ExplainedRenderRegion ex = new ExplainedRenderRegion(name, r);
				int entityCount = buf.readVarInt();
				for (int j = 0; j < entityCount; j++) {
					ex.entityAttachments.add(buf.readUuid());
				}
				int blockCount = buf.readVarInt();
				for (int j = 0; j < blockCount; j++) {
					ex.blockAttachments.add(buf.readLong());
				}
				int entityTypeCount = buf.readVarInt();
				for (int j = 0; j < entityTypeCount; j++) {
					ex.entityTypeAttachments.add(readId(buf, Registries.ENTITY_TYPE));
				}
				int beTypeCount = buf.readVarInt();
				for (int j = 0; j < beTypeCount; j++) {
					ex.beTypeAttachments.add(readId(buf, Registries.BLOCK_ENTITY_TYPE));
				}
				buf.readerIndex(start+len);
				bldr.add(ex);
			}
			return new FullState(bldr.build());
		}
		
		@Override
		public boolean valid() {
			return regions != null && !regions.isEmpty();
		}
		
		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
			for (var ex : regions) {
				tgt.add(ex.name, ex.reg);
				ex.entityAttachments.forEach(id -> tgt.attachEntity(ex.reg, id));
				ex.blockAttachments.forEach(pos -> tgt.attachBlock(ex.reg, pos));
				ex.entityTypeAttachments.forEach(id -> tgt.attachEntityType(ex.reg, id));
				ex.beTypeAttachments.forEach(id -> tgt.attachBlockEntityType(ex.reg, id));
			}
		}
	}
	
}
