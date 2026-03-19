package net.modfest.fireblanket.world.render_regions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.FireblanketConstants;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.AddRegion;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.AttachBlock;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.AttachEntity;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DestroyRegion;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DetachAll;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DetachBlock;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.DetachEntity;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.FullState;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.FullStateLegacy;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.InvalidCommand;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.RedefineRegion;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.RegistryRegionSyncRequest;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.Reset;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.UpdateRegionMetadata;
import net.modfest.fireblanket.world.render_regions.RenderRegion.Mode;

import java.util.BitSet;
import java.util.Collection;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public sealed interface RegionSyncRequest extends CustomPacketPayload permits InvalidCommand, FullState, Reset, AddRegion,
	DestroyRegion, DetachAll, AttachEntity, AttachBlock, DetachEntity, DetachBlock, RedefineRegion, FullStateLegacy,
	RegistryRegionSyncRequest, UpdateRegionMetadata {

	CustomPacketPayload.Type<RegionSyncRequest> ID = new CustomPacketPayload.Type<>(Fireblanket.REGIONS_UPDATE);

	StreamCodec<RegistryFriendlyByteBuf, RegionSyncRequest> CODEC = StreamCodec.ofMember(
		RegionSyncRequest::toPacket, RegionSyncRequest::read
	);

	enum RequestType {
		INVALID_COMMAND(InvalidCommand::read, "invalid_command"),
		FULL_STATE_LEGACY(FullStateLegacy::read, "full_state_legacy"),
		RESET(Reset::read, "reset"),
		ADD_REGION(AddRegion::read, "add_region"),
		DESTROY_REGION(DestroyRegion::read, "destroy_region"),
		DETACH_ALL(DetachAll::read, "detach_all"),
		ATTACH_ENTITY(AttachEntity::read, "attach_entity"),
		ATTACH_BLOCK(AttachBlock::read, "attach_block"),
		DETACH_ENTITY(DetachEntity::read, "detach_entity"),
		DETACH_BLOCK(DetachBlock::read, "detach_block"),
		REDEFINE_REGION(RedefineRegion::read, "redefine_region"),
		ATTACH_ENTITY_TYPE(AttachEntityType::read, "attach_entity_type"),
		DETACH_ENTITY_TYPE(DetachEntityType::read, "detach_entity_type"),
		ATTACH_BLOCK_ENTITY_TYPE(AttachBlockEntityType::read, "attach_block_entity_type"),
		DETACH_BLOCK_ENTITY_TYPE(DetachBlockEntityType::read, "detach_block_entity_type"),
		FULL_STATE(FullState::read, "full_state"),
		UPDATE_REGION_METADATA(UpdateRegionMetadata::read, "update_region"),
		;
		public static final ImmutableList<RequestType> VALUES = ImmutableList.copyOf(values());
		public final Function<FriendlyByteBuf, ? extends RegionSyncRequest> reader;
		public final Identifier id;

		RequestType(Function<FriendlyByteBuf, ? extends RegionSyncRequest> reader, String name) {
			this.reader = reader;
			this.id = FireblanketConstants.id(name);
		}

	}

	RequestType requestType();

	//  static self read(PacketByteBuf buf);
	void write(FriendlyByteBuf buf);

	void apply(RenderRegions tgt);

	boolean valid();

	sealed interface RegistryRegionSyncRequest<T> extends RegionSyncRequest permits AttachEntityType, DetachEntityType,
		AttachBlockEntityType, DetachBlockEntityType {

		Registry<T> registry();

		String name();

		Identifier id();

		@Override
		default boolean valid() {
			return name() != null && registry().getValue(id()) != null;
		}

		@Override
		default void write(FriendlyByteBuf buf) {
			buf.writeUtf(name());
			writeId(buf, registry(), id());
		}
	}

	default void toPacket(RegistryFriendlyByteBuf buf) {
		buf.writeByte(requestType().ordinal());
		write(buf);
	}

	@Override
	default Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	private static void writeRegion(FriendlyByteBuf buf, RenderRegion r) {
		buf.writeByte(r.mode().ordinal());
		buf.writeVarInt(r.minX()).writeVarInt(r.minY()).writeVarInt(r.minZ());
		buf.writeVarInt(r.maxX()).writeVarInt(r.maxY()).writeVarInt(r.maxZ());
	}

	private static RenderRegion readRegion(FriendlyByteBuf buf) {
		int modeId = buf.readUnsignedByte();
		if (modeId >= Mode.VALUES.size()) {
			Fireblanket.LOGGER.warn("Unknown region mode id {}", modeId);
			modeId = 0;
		}
		Mode mode = Mode.VALUES.get(modeId);
		return new RenderRegion(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
			buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
			mode);
	}

	private static <T> Identifier readId(FriendlyByteBuf buf, Registry<T> registry) {
		return registry.getKey(registry.byId(buf.readVarInt()));
	}

	private static <T> void writeId(FriendlyByteBuf buf, Registry<T> registry, Identifier id) {
		buf.writeVarInt(registry.getId(registry.getValue(id)));
	}

	private static <T> void readTo(FriendlyByteBuf buf, Collection<T> collection, Function<FriendlyByteBuf, T> function) {
		final int len = buf.readVarInt();
		for (int i = 0; i < len; i++) {
			collection.add(function.apply(buf));
		}
	}

	private static void readTo(FriendlyByteBuf buf, LongCollection collection) {
		final int len = buf.readVarInt();
		for (int i = 0; i < len; i++) {
			collection.add(buf.readLong());
		}
	}

	private static <T> void writeTo(FriendlyByteBuf buf, Collection<T> collection, BiConsumer<FriendlyByteBuf, T> consumer) {
		buf.writeVarInt(collection.size());
		for (final T t : collection) {
			consumer.accept(buf, t);
		}
	}

	private static void writeTo(FriendlyByteBuf buf, LongCollection collection) {
		buf.writeVarInt(collection.size());
		LongIterator itr = collection.iterator();
		while (itr.hasNext()) {
			buf.writeLong(itr.nextLong());
		}
	}

	static RegionSyncRequest read(RegistryFriendlyByteBuf buf) {
		int tid = buf.readUnsignedByte();
		if (tid >= RequestType.VALUES.size()) {
			int len = buf.readableBytes();
			buf.skipBytes(len);
			Fireblanket.LOGGER.warn("Unknown region sync command id {}, had {} bytes", tid, len);
			return new InvalidCommand();
		}
		RequestType t = RequestType.VALUES.get(tid);
		return t.reader.apply(buf);
	}

	record InvalidCommand() implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.INVALID_COMMAND;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			Fireblanket.LOGGER.warn("Writing an invalid command");
		}

		public static InvalidCommand read(FriendlyByteBuf buf) {
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

	record FullStateLegacy(ImmutableMap<String, RenderRegion> regions, ImmutableMultimap<RenderRegion, UUID> entityAttachments, ImmutableMultimap<RenderRegion, Long> blockAttachments) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.FULL_STATE_LEGACY;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			throw new UnsupportedOperationException();
		}

		public static FullStateLegacy read(FriendlyByteBuf buf) {
			ImmutableMap.Builder<String, RenderRegion> regionsBldr = ImmutableMap.builder();
			ImmutableMultimap.Builder<RenderRegion, UUID> entityAttachmentsBldr = ImmutableMultimap.builder();
			ImmutableMultimap.Builder<RenderRegion, Long> blockAttachmentsBldr = ImmutableMultimap.builder();
			int regionCount = buf.readVarInt();
			for (int i = 0; i < regionCount; i++) {
				String name = buf.readUtf();
				RenderRegion r = readRegion(buf);
				regionsBldr.put(name, r);
				int entityCount = buf.readVarInt();
				for (int j = 0; j < entityCount; j++) {
					entityAttachmentsBldr.put(r, buf.readUUID());
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

	record Reset(boolean valid) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.RESET;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeInt(0xDEADDEAD);
		}

		public static Reset read(FriendlyByteBuf buf) {
			return new Reset(buf.readInt() == 0xDEADDEAD);
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.clear();
		}

	}

	record AddRegion(String name, RenderRegion region) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.ADD_REGION;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			writeRegion(buf, region);
		}

		public static AddRegion read(FriendlyByteBuf buf) {
			return new AddRegion(buf.readUtf(), readRegion(buf));
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

	record DestroyRegion(String name) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.DESTROY_REGION;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
		}

		public static DestroyRegion read(FriendlyByteBuf buf) {
			return new DestroyRegion(buf.readUtf());
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

	record DetachAll(String name) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.DETACH_ALL;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
		}

		public static DetachAll read(FriendlyByteBuf buf) {
			return new DetachAll(buf.readUtf());
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

	record AttachEntity(String name, UUID entity) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.ATTACH_ENTITY;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			buf.writeUUID(entity);
		}

		public static AttachEntity read(FriendlyByteBuf buf) {
			return new AttachEntity(buf.readUtf(), buf.readUUID());
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

	record AttachBlock(String name, long pos) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.ATTACH_BLOCK;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			buf.writeLong(pos);
		}

		public static AttachBlock read(FriendlyByteBuf buf) {
			return new AttachBlock(buf.readUtf(), buf.readLong());
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

	record DetachEntity(String name, UUID entity) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.DETACH_ENTITY;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			buf.writeUUID(entity);
		}

		public static DetachEntity read(FriendlyByteBuf buf) {
			return new DetachEntity(buf.readUtf(), buf.readUUID());
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

	record DetachBlock(String name, long pos) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.DETACH_BLOCK;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			buf.writeLong(pos);
		}

		public static DetachBlock read(FriendlyByteBuf buf) {
			return new DetachBlock(buf.readUtf(), buf.readLong());
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

	record RedefineRegion(String name, RenderRegion region) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.REDEFINE_REGION;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeUtf(name);
			writeRegion(buf, region);
		}

		public static RedefineRegion read(FriendlyByteBuf buf) {
			return new RedefineRegion(buf.readUtf(), readRegion(buf));
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

	record AttachEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<EntityType<?>> {

		@Override
		public RequestType requestType() {
			return RequestType.ATTACH_ENTITY_TYPE;
		}

		@Override
		public Registry<EntityType<?>> registry() {
			return BuiltInRegistries.ENTITY_TYPE;
		}

		public static AttachEntityType read(FriendlyByteBuf buf) {
			return new AttachEntityType(buf.readUtf(), readId(buf, BuiltInRegistries.ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachEntityType(tgt.getByName(name), id);
		}

	}

	record DetachEntityType(String name, Identifier id) implements RegistryRegionSyncRequest<EntityType<?>> {

		@Override
		public RequestType requestType() {
			return RequestType.DETACH_ENTITY_TYPE;
		}

		@Override
		public Registry<EntityType<?>> registry() {
			return BuiltInRegistries.ENTITY_TYPE;
		}

		public static DetachEntityType read(FriendlyByteBuf buf) {
			return new DetachEntityType(buf.readUtf(), readId(buf, BuiltInRegistries.ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachEntityType(tgt.getByName(name), id);
		}

	}

	record AttachBlockEntityType(String name,
								 Identifier id) implements RegistryRegionSyncRequest<BlockEntityType<?>> {

		@Override
		public RequestType requestType() {
			return RequestType.ATTACH_BLOCK_ENTITY_TYPE;
		}

		@Override
		public Registry<BlockEntityType<?>> registry() {
			return BuiltInRegistries.BLOCK_ENTITY_TYPE;
		}

		public static AttachBlockEntityType read(FriendlyByteBuf buf) {
			return new AttachBlockEntityType(buf.readUtf(), readId(buf, BuiltInRegistries.BLOCK_ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.attachBlockEntityType(tgt.getByName(name), id);
		}

	}

	record DetachBlockEntityType(String name,
								 Identifier id) implements RegistryRegionSyncRequest<BlockEntityType<?>> {

		@Override
		public RequestType requestType() {
			return RequestType.DETACH_BLOCK_ENTITY_TYPE;
		}

		@Override
		public Registry<BlockEntityType<?>> registry() {
			return BuiltInRegistries.BLOCK_ENTITY_TYPE;
		}

		public static DetachBlockEntityType read(FriendlyByteBuf buf) {
			return new DetachBlockEntityType(buf.readUtf(), readId(buf, BuiltInRegistries.BLOCK_ENTITY_TYPE));
		}

		@Override
		public void apply(RenderRegions tgt) {
			tgt.detachBlockEntityType(tgt.getByName(name), id);
		}

	}

	record FullState(ImmutableList<ExplainedRenderRegion> regions) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.FULL_STATE;
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			buf.writeVarInt(regions.size());
			for (var ex : regions) {
				buf.writeUtf(ex.name);
				int sizePos = buf.writerIndex();
				buf.writeMedium(0);
				int start = buf.writerIndex();
				RenderRegion r = ex.reg;
				writeRegion(buf, r);

				writeTo(buf, ex.entityAttachments, (b, uuid) -> b.writeUUID(uuid));
				writeTo(buf, ex.blockAttachments);
				writeTo(buf, ex.entityTypeAttachments, (b, id) -> writeId(b, BuiltInRegistries.ENTITY_TYPE, id));
				writeTo(buf, ex.beTypeAttachments, (b, id) -> writeId(b, BuiltInRegistries.BLOCK_ENTITY_TYPE, id));

				buf.writeBitSet(ex.getMeta());

				int len = buf.writerIndex() - start;
				buf.markWriterIndex();
				buf.writerIndex(sizePos);
				buf.writeMedium(len);
				buf.resetWriterIndex();
			}
		}

		public static FullState read(FriendlyByteBuf buf) {
			ImmutableList.Builder<ExplainedRenderRegion> bldr = ImmutableList.builder();
			int regionCount = buf.readVarInt();
			for (int i = 0; i < regionCount; i++) {
				String name = buf.readUtf();
				int len = buf.readUnsignedMedium();
				int start = buf.readerIndex();
				RenderRegion r = readRegion(buf);
				ExplainedRenderRegion ex = new ExplainedRenderRegion(name, r);

				readTo(buf, ex.entityAttachments, b -> b.readUUID());
				readTo(buf, ex.blockAttachments);
				readTo(buf, ex.entityTypeAttachments, b -> readId(buf, BuiltInRegistries.ENTITY_TYPE));
				readTo(buf, ex.beTypeAttachments, b -> readId(buf, BuiltInRegistries.BLOCK_ENTITY_TYPE));

				extension:
				{
					if (buf.readerIndex() == len + start) {
						break extension;
					}

					ex.applyMeta(buf.readBitSet());
				}

				buf.readerIndex(start + len);
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
				tgt.applyMeta(ex);
			}
		}
	}

	record UpdateRegionMetadata(String name, BitSet meta) implements RegionSyncRequest {

		@Override
		public RequestType requestType() {
			return RequestType.UPDATE_REGION_METADATA;
		}

		@Override
		public void write(final FriendlyByteBuf buf) {
			buf.writeUtf(name);
			buf.writeBitSet(meta);
		}

		public static UpdateRegionMetadata read(final FriendlyByteBuf buf) {
			return new UpdateRegionMetadata(buf.readUtf(), buf.readBitSet());
		}

		@Override
		public void apply(final RenderRegions tgt) {
			final RenderRegion region = tgt.getByName(name());

			tgt.setEntityTypeAttachmentsInverted(region, meta.get(0));
			tgt.setEntityTypeBoxBounded(region, meta.get(1));
			tgt.setBeTypeAttachmentsInverted(region, meta.get(2));
			tgt.setBeTypeBoxBounded(region, meta.get(3));
		}

		@Override
		public boolean valid() {
			return name != null;
		}
	}

}
