package net.modfest.fireblanket.render_regions;

import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.net.writer.ServerPacketWriter;
import net.modfest.fireblanket.render_regions.RenderRegion.Mode;

import net.modfest.fireblanket.render_regions.RegionSyncCommand.*;

public sealed interface RegionSyncCommand extends ServerPacketWriter permits InvalidCommand, FullState, Reset, AddRegion,
        DestroyRegion, DetachAll, AttachEntity, AttachBlock, DetachEntity, DetachBlock {

    public enum Type {
        INVALID_COMMAND(InvalidCommand::read),
        FULL_STATE(FullState::read),
        RESET(Reset::read),
        ADD_REGION(AddRegion::read),
        DESTROY_REGION(DestroyRegion::read),
        DETACH_ALL(DetachAll::read),
        ATTACH_ENTITY(AttachEntity::read),
        ATTACH_BLOCK(AttachBlock::read),
        DETACH_ENTITY(DetachEntity::read),
        DETACH_BLOCK(DetachBlock::read),
        ;
        public static final ImmutableList<Type> VALUES = ImmutableList.copyOf(values());
        public final Function<PacketByteBuf, ? extends RegionSyncCommand> reader;

        Type(Function<PacketByteBuf, ? extends RegionSyncCommand> reader) {
            this.reader = reader;
        }
        
    }
    
    Type type();
    
//  static self read(PacketByteBuf buf);
    void write(PacketByteBuf buf);
    
    void apply(RenderRegions tgt);
    
    boolean valid();
    
    @Override
    default void write(PacketByteBuf buf, Identifier packetId) {
        buf.writeByte(type().ordinal());
        write(buf);
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
    
    static RegionSyncCommand read(PacketByteBuf buf) {
        int tid = buf.readUnsignedByte();
        if (tid >= Type.VALUES.size()) {
            Fireblanket.LOGGER.warn("Unknown region sync command id "+tid);
            return new InvalidCommand();
        }
        Type t = Type.VALUES.get(tid);
        return t.reader.apply(buf);
    }
    
    public record InvalidCommand() implements RegionSyncCommand {

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
    
    public record FullState(ImmutableMap<String, RenderRegion> regions,
            ImmutableMultimap<RenderRegion, UUID> entityAttachments,
            ImmutableMultimap<RenderRegion, Long> blockAttachments) implements RegionSyncCommand {
        
        public FullState(RenderRegions toCopy) {
            this(ImmutableMap.copyOf(toCopy.getRegionsByName()),
                    ImmutableMultimap.copyOf(toCopy.getAllEntityAttachments()),
                    ImmutableMultimap.copyOf(toCopy.getAllBlockEntityAttachments()));
        }
        
        @Override
        public Type type() {
            return Type.FULL_STATE;
        }

        @Override
        public void write(PacketByteBuf buf) {
            buf.writeVarInt(regions.size());
            for (var en : regions.entrySet()) {
                buf.writeString(en.getKey());
                RenderRegion r = en.getValue();
                writeRegion(buf, r);
                var ea = entityAttachments.get(r);
                buf.writeVarInt(ea.size());
                for (UUID id : ea) {
                    buf.writeUuid(id);
                }
                var ba = blockAttachments.get(r);
                buf.writeVarInt(ba.size());
                for (long pos : ba) {
                    buf.writeLong(pos);
                }
            }
        }
        
        public static FullState read(PacketByteBuf buf) {
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
            return new FullState(regionsBldr.build(), entityAttachmentsBldr.build(), blockAttachmentsBldr.build());
        }
        
        @Override
        public boolean valid() {
            return regions != null && entityAttachments != null && blockAttachments != null
                    && !regions.isEmpty();
        }
        
        @Override
        public void apply(RenderRegions tgt) {
            tgt.clear();
            regions.forEach(tgt::addRegion);
            entityAttachments.forEach(tgt::attachEntity);
            blockAttachments.forEach(tgt::attachBlockEntity);
        }
    }
    
    public record Reset(boolean valid) implements RegionSyncCommand {

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
    
    public record AddRegion(String name, RenderRegion region) implements RegionSyncCommand {

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
            tgt.addRegion(name, region);
        }
        
    }
    
    public record DestroyRegion(String name) implements RegionSyncCommand {

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
            tgt.removeRegion(tgt.getByName(name));
        }
        
    }
    
    public record DetachAll(String name) implements RegionSyncCommand {

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
    
    public record AttachEntity(String name, UUID entity) implements RegionSyncCommand {

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
    
    public record AttachBlock(String name, long pos) implements RegionSyncCommand {

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
            tgt.attachBlockEntity(tgt.getByName(name), pos);
        }
        
    }
    
    public record DetachEntity(String name, UUID entity) implements RegionSyncCommand {

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
    
    public record DetachBlock(String name, long pos) implements RegionSyncCommand {

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
            tgt.detachBlockEntity(tgt.getByName(name), pos);
        }
        
    }
    
}
