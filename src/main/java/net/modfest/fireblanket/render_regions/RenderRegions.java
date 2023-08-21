package net.modfest.fireblanket.render_regions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.modfest.fireblanket.render_regions.RegionSyncRequest.FullState;
import net.modfest.fireblanket.render_regions.RenderRegion.Mode;

public class RenderRegions {

	private final BiMap<String, ExplainedRenderRegion> regionsByName = HashBiMap.create();
	private final Reference2ReferenceMap<RenderRegion, ExplainedRenderRegion> explaineds = new Reference2ReferenceOpenHashMap<>();
	private final Long2ReferenceMultimap<ExplainedRenderRegion> regionsByChunkSection = new Long2ReferenceMultimap<>();
	
	private final Long2ReferenceMultimap<ExplainedRenderRegion> blockRegions = new Long2ReferenceMultimap<>();
	private final ListMultimap<UUID, ExplainedRenderRegion> entityRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	private final ListMultimap<Identifier, ExplainedRenderRegion> exclusiveEntityTypeRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	private final ListMultimap<Identifier, ExplainedRenderRegion> exclusiveBeTypeRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	
	private final Runnable dirtyListener;
	private final Consumer<RegionSyncRequest> syncer;
	
	private boolean dontSync = false;
	
	private int era = 0;

	public RenderRegions() {
		this(null, null);
	}

	public RenderRegions(Runnable dirtyListener, Consumer<RegionSyncRequest> syncer) {
		this.dirtyListener = dirtyListener;
		this.syncer = syncer;
	}
	
	public void markDirty() {
		era++;
		if (dirtyListener != null) dirtyListener.run();
	}
	
	public void sync(Supplier<RegionSyncRequest> supplier) {
		if (dontSync) return;
		if (syncer != null) {
			var req = supplier.get();
			if (!req.valid()) return;
			syncer.accept(req);
		}
	}

	public void add(String name, RenderRegion region) {
		if (region == null) throw new IllegalArgumentException("region cannot be null");
		ExplainedRenderRegion ex = new ExplainedRenderRegion(name, region);
		ExplainedRenderRegion old = regionsByName.put(name, ex);
		if (old != null) {
			remove(old.reg);
		}
		explaineds.put(region, ex);
		addToGlobal(ex);
		if (region.mode() == Mode.DENY) ex.blanketDeny = true;
		markDirty();
		sync(() -> new RegionSyncRequest.AddRegion(name, region));
	}
	
	public void remove(RenderRegion region) {
		remove(region, true);
	}
	
	private void remove(RenderRegion region, boolean clear) {
		if (region == null) return;
		detachAll(region, clear);
		ExplainedRenderRegion ex = explaineds.remove(region);
		String name = regionsByName.inverse().remove(ex);
		removeFromGlobal(ex);
		markDirty();
		if (name != null) {
			sync(() -> new RegionSyncRequest.DestroyRegion(name));
		}
	}

	public void redefine(String name, RenderRegion nw) {
		RenderRegion ol = getByName(name);
		if (ol == null) {
			add(name, nw);
			return;
		}
		ExplainedRenderRegion exol = explaineds.get(ol);
		try {
			dontSync = true;
			remove(ol, false);
			ExplainedRenderRegion exnw = new ExplainedRenderRegion(name, nw);
			explaineds.put(nw, exnw);
			regionsByName.put(name, exnw);
			addToGlobal(exnw);
			if (nw.mode() == Mode.DENY) exnw.blanketDeny = true;
			exol.blockAttachments.forEach(pos -> attachBlock(nw, pos));
			exol.entityAttachments.forEach(id -> attachEntity(nw, id));
			exol.beTypeAttachments.forEach(id -> attachBlockEntityType(nw, id));
			exol.entityTypeAttachments.forEach(id -> attachEntityType(nw, id));
			dontSync = false;
			sync(() -> new RegionSyncRequest.RedefineRegion(name, nw));
		} finally {
			dontSync = false;
		}
	}
	
	public FullState toPacket() {
		return new FullState(ImmutableList.copyOf(Iterables.transform(explaineds.values(), ExplainedRenderRegion::copy)));
	}
	
	public Map<String, RenderRegion> getRegionsByName() {
		return Maps.transformValues(regionsByName, ex -> ex.reg);
	}
	
	private void removeFromGlobal(ExplainedRenderRegion ex) {
		ex.reg.affectedChunkLongs().forEach(l -> {
			regionsByChunkSection.remove(l, ex);
		});
	}
	
	private void addToGlobal(ExplainedRenderRegion ex) {
		ex.reg.affectedChunkLongs().forEach(l -> {
			regionsByChunkSection.put(l, ex);
		});
	}

	public void clear() {
		regionsByName.clear();
		regionsByChunkSection.clear();
		blockRegions.clear();
		entityRegions.clear();
		exclusiveEntityTypeRegions.clear();
		exclusiveBeTypeRegions.clear();
		explaineds.clear();
		markDirty();
		sync(() -> new RegionSyncRequest.Reset(true));
	}
	
	public void attachEntity(RenderRegion region, Entity e) {
		attachEntity(region, e.getUuid());
	}
	
	public void attachEntity(RenderRegion region, UUID id) {
		if (region == null) return;
		ExplainedRenderRegion ex = explaineds.get(region);
		entityRegions.put(id, ex);
		ex.entityAttachments.add(id);
		markDirty();
		sync(() -> new RegionSyncRequest.AttachEntity(ex.name, id));
	}
	
	public void attachEntityType(RenderRegion region, EntityType<?> type) {
		attachEntityType(region, EntityType.getId(type));
	}
	
	public void attachEntityType(RenderRegion region, Identifier id) {
		if (region == null || id == null) return;
		ExplainedRenderRegion ex = explaineds.get(region);
		ex.entityTypeAttachments.add(id);
		if (region.mode() == Mode.EXCLUSIVE) {
			exclusiveEntityTypeRegions.put(id, ex);
		}
		ex.blanketDeny = false;
		markDirty();
		sync(() -> new RegionSyncRequest.AttachEntityType(ex.name, id));
	}
	
	public void attachBlock(RenderRegion region, BlockEntity be) {
		attachBlock(region, be.getPos().asLong());
	}
	
	public void attachBlock(RenderRegion region, long pos) {
		if (region == null) return;
		ExplainedRenderRegion ex = explaineds.get(region);
		blockRegions.put(pos, ex);
		ex.blockAttachments.add(pos);
		ex.blanketDeny = false;
		markDirty();
		sync(() -> new RegionSyncRequest.AttachBlock(ex.name, pos));
	}
	
	public void attachBlockEntityType(RenderRegion region, BlockEntityType<?> type) {
		attachBlockEntityType(region, BlockEntityType.getId(type));
	}
	
	public void attachBlockEntityType(RenderRegion region, Identifier id) {
		if (region == null || id == null) return;
		ExplainedRenderRegion ex = explaineds.get(region);
		ex.beTypeAttachments.add(id);
		if (region.mode() == Mode.EXCLUSIVE) {
			exclusiveBeTypeRegions.put(id, ex);
		}
		ex.blanketDeny = false;
		markDirty();
		sync(() -> new RegionSyncRequest.AttachBlockEntityType(ex.name, id));
	}
	
	public boolean detachEntity(RenderRegion region, Entity e) {
		return detachEntity(region, e.getUuid());
	}
	
	public boolean detachEntity(RenderRegion region, UUID id) {
		if (region == null) return false;
		ExplainedRenderRegion ex = explaineds.get(region);
		boolean success = entityRegions.remove(id, ex);
		ex.entityAttachments.remove(id);
		checkBlanketDeny(ex);
		sync(() -> new RegionSyncRequest.DetachEntity(ex.name, id));
		return success;
	}
	
	public void detachEntityType(RenderRegion region, EntityType<?> type) {
		detachEntityType(region, Registries.ENTITY_TYPE.getId(type));
	}
	
	public void detachEntityType(RenderRegion region, Identifier id) {
		if (region == null) return;
		ExplainedRenderRegion ex = explaineds.get(region);
		ex.entityTypeAttachments.remove(id);
		exclusiveEntityTypeRegions.remove(id, ex);
		checkBlanketDeny(ex);
		markDirty();
		sync(() -> new RegionSyncRequest.DetachEntityType(ex.name, id));
	}
	
	public boolean detachBlock(RenderRegion region, BlockEntity be) {
		return detachBlock(region, be.getPos().asLong());
	}
	
	public boolean detachBlock(RenderRegion region, long pos) {
		if (region == null) return false;
		ExplainedRenderRegion ex = explaineds.get(region);
		boolean success = blockRegions.remove(pos, ex);
		ex.blockAttachments.remove(pos);
		checkBlanketDeny(ex);
		sync(() -> new RegionSyncRequest.DetachBlock(ex.name, pos));
		return success;
	}
	
	public void detachBlockEntityType(RenderRegion region, BlockEntityType<?> type) {
		detachBlockEntityType(region, Registries.BLOCK_ENTITY_TYPE.getId(type));
	}
	
	public void detachBlockEntityType(RenderRegion region, Identifier id) {
		if (region == null) return;
		ExplainedRenderRegion ex = explaineds.get(region);
		ex.beTypeAttachments.remove(id);
		exclusiveBeTypeRegions.remove(id, ex);
		checkBlanketDeny(ex);
		markDirty();
		sync(() -> new RegionSyncRequest.DetachBlockEntityType(ex.name, id));
	}
	
	private void checkBlanketDeny(ExplainedRenderRegion ex) {
		if (ex.entityAttachments.isEmpty()
				&& ex.blockAttachments.isEmpty()
				&& ex.entityTypeAttachments.isEmpty()
				&& ex.beTypeAttachments.isEmpty()) {
			ex.blanketDeny = true;
		}
	}


	public int detachAll(RenderRegion region) {
		return detachAll(region, true);
	}
	
	private int detachAll(RenderRegion region, boolean clear) {
		if (region == null) return 0;
		ExplainedRenderRegion ex = explaineds.get(region);
		int count = 0;
		for (UUID u : ex.entityAttachments) {
			entityRegions.remove(u, region);
			count++;
		}
		if (clear) ex.entityAttachments.clear();
		LongIterator iter = LongIterators.asLongIterator(ex.blockAttachments.iterator());
		while (iter.hasNext()) {
			blockRegions.remove(iter.nextLong(), region);
			count++;
		}
		if (clear) ex.blockAttachments.clear();
		if (ex.reg.mode() == Mode.EXCLUSIVE) {
			for (Identifier id : ex.beTypeAttachments) {
				exclusiveBeTypeRegions.remove(id, ex);
			}
			for (Identifier id : ex.entityTypeAttachments) {
				exclusiveEntityTypeRegions.remove(id, ex);
			}
		}
		if (clear) ex.beTypeAttachments.clear();
		if (clear) ex.entityTypeAttachments.clear();
		if (clear && region.mode() == Mode.DENY) ex.blanketDeny = true;
		sync(() -> new RegionSyncRequest.DetachAll(ex.name));
		return count;
	}

	public RenderRegion getByName(String name) {
		ExplainedRenderRegion ex = regionsByName.get(name);
		if (ex == null) return null;
		return ex.reg;
	}
	
	public String getName(RenderRegion region) {
		return regionsByName.inverse().get(explaineds.get(region));
	}
	
	public LongSet getBlockAttachments(RenderRegion region) {
		ExplainedRenderRegion ex = explaineds.get(region);
		if (ex == null) return LongSets.emptySet();
		return ex.blockAttachments;
	}
	
	public Set<UUID> getEntityAttachments(RenderRegion region) {
		ExplainedRenderRegion ex = explaineds.get(region);
		if (ex == null) return Collections.emptySet();
		return ex.entityAttachments;
	}
	
	public Set<Identifier> getBlockEntityTypeAttachments(RenderRegion region) {
		ExplainedRenderRegion ex = explaineds.get(region);
		if (ex == null) return Collections.emptySet();
		return ex.beTypeAttachments;
	}
	
	public Set<Identifier> getEntityTypeAttachments(RenderRegion region) {
		ExplainedRenderRegion ex = explaineds.get(region);
		if (ex == null) return Collections.emptySet();
		return ex.entityTypeAttachments;
	}
	
	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, BlockEntity be) {
		long viewerPos = BlockPos.asLong((int)viewerX, (int)viewerY, (int)viewerZ);
		if (be instanceof RegionSubject rs) {
			Boolean cached = rs.fireblanket$cachedShouldRender(era, viewerPos);
			if (cached != null) return cached;
		}
		boolean res = shouldRender(ex -> ex.beTypeAttachments, BlockEntityType.getId(be.getType()),
				exclusiveBeTypeRegions,
				blockRegions.get(be.getPos().asLong()), viewerX, viewerY, viewerZ);
		if (be instanceof RegionSubject rs) {
			rs.fireblanket$setCachedState(era, viewerPos, res);
		}
		return res;
	}
	
	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, Entity e) {
		if (e instanceof PlayerEntity && e.shouldRenderName()) return true;
		long viewerPos = BlockPos.asLong((int)viewerX, (int)viewerY, (int)viewerZ);
		if (e instanceof RegionSubject rs) {
			Boolean cached = rs.fireblanket$cachedShouldRender(era, viewerPos);
			if (cached != null) return cached;
		}
		boolean res = shouldRender(ex -> ex.entityTypeAttachments, EntityType.getId(e.getType()),
				exclusiveEntityTypeRegions,
				entityRegions.get(e.getUuid()), viewerX, viewerY, viewerZ);
		if (e instanceof RegionSubject rs) {
			rs.fireblanket$setCachedState(era, viewerPos, res);
		}
		return res;
	}

	private boolean shouldRender(Function<ExplainedRenderRegion, Set<Identifier>> typeAttachments, Identifier type,
			ListMultimap<Identifier, ExplainedRenderRegion> exclusiveTypeRegions,
			Iterable<ExplainedRenderRegion> assignedRegions, double viewerX, double viewerY, double viewerZ) {
		boolean anyExclusive = false;
		int vX = (int)viewerX;
		int vY = (int)viewerY;
		int vZ = (int)viewerZ;
		long chunkSect = ChunkSectionPos.asLong(vX>>4, vY>>4, vZ>>4);
		
		boolean blanketDeny = false;
		
		// check explicit id/position first
		for (var rr : assignedRegions) {
			if (rr == null) continue;
			if (rr.reg.mode() == Mode.EXCLUSIVE) {
				anyExclusive = true;
				if (rr.reg.contains(viewerX, viewerY, viewerZ)) {
					return true;
				}
			} else if (rr.reg.contains(viewerX, viewerY, viewerZ)) {
				return rr.reg.mode() == Mode.ALLOW;
			}
		}
		
		// fall through to area regions that may have a type match
		boolean permitted = false;
		for (var rr : regionsByChunkSection.get(chunkSect)) {
			Boolean containsCache = null;
			if (rr.reg.mode() == Mode.EXCLUSIVE && (containsCache = typeAttachments.apply(rr).contains(type))) {
				anyExclusive = true;
			}
			if (rr.reg.contains(viewerX, viewerY, viewerZ)) {
				if (containsCache == null ? typeAttachments.apply(rr).contains(type) : containsCache) {
					if (rr.reg.mode() == Mode.DENY) {
						return false;
					} else {
						permitted = true;
					}
				} else if (!blanketDeny && rr.reg.mode() == Mode.DENY && rr.blanketDeny) {
					blanketDeny = true;
				}
			}
		}
		if (permitted) return true;
		
		// exclusive regions not matched above must mean reject
		for (var rr : exclusiveTypeRegions.get(type)) {
			if (!rr.reg.contains(viewerX, viewerY, viewerZ)) {
				return false;
			}
		}
		if (anyExclusive) return false;
		return !blanketDeny;
	}
	
	public void readNbt(NbtCompound nbt) {
		for (String name : nbt.getKeys()) {
			NbtCompound cmp = nbt.getCompound(name);
			Mode m = Mode.valueOf(cmp.getString("Mode"));
			int[] box = cmp.getIntArray("Box");
			RenderRegion r = new RenderRegion(box[0], box[1], box[2], box[3], box[4], box[5], m);
			add(name, r);
			int[] entities = cmp.getIntArray("EAtt");
			for (int i = 0; i < entities.length; i += 4) {
				attachEntity(r, Uuids.toUuid(Arrays.copyOfRange(entities, i, i+4)));
			}
			long[] blockentities = cmp.getLongArray("BEAtt");
			for (long l : blockentities) {
				attachBlock(r, l);
			}
			for (NbtElement ele : cmp.getList("ETAtt", NbtElement.STRING_TYPE)) {
				attachEntityType(r, Identifier.tryParse(ele.asString()));
			}
			for (NbtElement ele : cmp.getList("BETAtt", NbtElement.STRING_TYPE)) {
				attachBlockEntityType(r, Identifier.tryParse(ele.asString()));
			}
		}
	}
	
	public void writeNbt(NbtCompound nbt) {
		for (var ex : explaineds.values()) {
			RenderRegion r = ex.reg;
			NbtCompound cmp = new NbtCompound();
			cmp.putString("Mode", r.mode().name());
			cmp.putIntArray("Box", new int[] { r.minX(), r.minY(), r.minZ(), r.maxX(), r.maxY(), r.maxZ() });
			IntList entities = new IntArrayList();
			for (var entityId : ex.entityAttachments) {
				entities.addAll(IntList.of(Uuids.toIntArray(entityId)));
			}
			cmp.putIntArray("EAtt", entities.toIntArray());
			cmp.putLongArray("BEAtt", ex.blockAttachments.toLongArray());
			NbtList entityTypes = new NbtList();
			for (Identifier id : ex.entityTypeAttachments) {
				entityTypes.add(NbtString.of(id.toString()));
			}
			cmp.put("ETAtt", entityTypes);
			NbtList beTypes = new NbtList();
			for (Identifier id : ex.beTypeAttachments) {
				beTypes.add(NbtString.of(id.toString()));
			}
			cmp.put("BETAtt", beTypes);
			nbt.put(ex.name, cmp);
		}
	}
	
}
