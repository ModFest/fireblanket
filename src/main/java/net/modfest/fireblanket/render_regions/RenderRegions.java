package net.modfest.fireblanket.render_regions;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.modfest.fireblanket.render_regions.RenderRegion.Mode;

public class RenderRegions {

	private final BiMap<String, RenderRegion> regionsByName = HashBiMap.create();
	private final Long2ReferenceMultimap<RenderRegion> denyRegionsByChunkSection = new Long2ReferenceMultimap<>();
	
	private final Long2ReferenceMultimap<RenderRegion> blockRegions = new Long2ReferenceMultimap<>();
	private final ListMultimap<UUID, RenderRegion> entityRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	
	private final SetMultimap<RenderRegion, UUID> entityAttachments = Multimaps.newSetMultimap(new Reference2ReferenceOpenHashMap<>(), ObjectOpenHashSet::new);
	private final SetMultimap<RenderRegion, Long> blockAttachments = Multimaps.newSetMultimap(new Reference2ReferenceOpenHashMap<>(), LongOpenHashSet::new);
	
	private final Runnable dirtyListener;
	private final Consumer<RegionSyncRequest> syncer;
	
	private boolean dontSync = false;

	public RenderRegions() {
		this(null, null);
	}

	public RenderRegions(Runnable dirtyListener, Consumer<RegionSyncRequest> syncer) {
		this.dirtyListener = dirtyListener;
		this.syncer = syncer;
	}
	
	public void markDirty() {
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
		RenderRegion old = regionsByName.put(name, region);
		if (old != null) {
			remove(old);
		}
		addToGlobalDeny(region);
		markDirty();
		sync(() -> new RegionSyncRequest.AddRegion(name, region));
	}
	
	public void remove(RenderRegion region) {
		if (region == null) return;
		String name = regionsByName.inverse().remove(region);
		removeFromGlobalDeny(region);
		detachAll(region, false);
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
		LongSet ba = new LongArraySet(getBlockAttachments(ol));
		Set<UUID> ea = new ObjectArraySet<>(getEntityAttachments(ol));
		try {
			dontSync = true;
			remove(ol);
			regionsByName.put(name, nw);
			if (ba.isEmpty() && ea.isEmpty()) {
				addToGlobalDeny(nw);
			} else {
				ba.forEach(pos -> attachBlock(nw, pos));
				ea.forEach(id -> attachEntity(nw, id));
			}
			dontSync = false;
			sync(() -> new RegionSyncRequest.RedefineRegion(name, nw));
		} finally {
			dontSync = false;
		}
	}
	
	private void removeFromGlobalDeny(RenderRegion region) {
		if (region.mode() != Mode.DENY) return;
		region.affectedChunkLongs().forEach(l -> {
			denyRegionsByChunkSection.remove(l, region);
		});
	}
	
	private void addToGlobalDeny(RenderRegion region) {
		if (region.mode() != Mode.DENY) return;
		region.affectedChunkLongs().forEach(l -> {
			denyRegionsByChunkSection.put(l, region);
		});
	}

	public void clear() {
		regionsByName.clear();
		denyRegionsByChunkSection.clear();
		blockRegions.clear();
		entityRegions.clear();
		entityAttachments.clear();
		blockAttachments.clear();
		lastPos = Long.MIN_VALUE;
		lastShouldRender = true;
		markDirty();
		sync(() -> new RegionSyncRequest.Reset(true));
	}
	
	public void attachEntity(RenderRegion region, Entity e) {
		attachEntity(region, e.getUuid());
	}
	
	public void attachEntity(RenderRegion region, UUID id) {
		if (region == null) return;
		if (!entityAttachments.containsKey(region)) {
			removeFromGlobalDeny(region);
		}
		entityRegions.put(id, region);
		entityAttachments.put(region, id);
		markDirty();
		sync(() -> new RegionSyncRequest.AttachEntity(getName(region), id));
	}
	
	public void attachBlock(RenderRegion region, BlockEntity be) {
		attachBlock(region, be.getPos().asLong());
	}
	
	public void attachBlock(RenderRegion region, long pos) {
		if (region == null) return;
		if (!blockAttachments.containsKey(region)) {
			removeFromGlobalDeny(region);
		}
		blockRegions.put(pos, region);
		blockAttachments.put(region, pos);
		markDirty();
		sync(() -> new RegionSyncRequest.AttachBlock(getName(region), pos));
	}
	
	public boolean detachEntity(RenderRegion region, Entity e) {
		return detachEntity(region, e.getUuid());
	}
	
	public boolean detachEntity(RenderRegion region, UUID id) {
		if (region == null) return false;
		boolean success = entityRegions.remove(id, region);
		entityAttachments.remove(region, id);
		if (!entityAttachments.containsKey(region)) {
			addToGlobalDeny(region);
		}
		sync(() -> new RegionSyncRequest.DetachEntity(getName(region), id));
		return success;
	}
	
	public boolean detachBlock(RenderRegion region, BlockEntity be) {
		return detachBlock(region, be.getPos().asLong());
	}
	
	public boolean detachBlock(RenderRegion region, long pos) {
		if (region == null) return false;
		boolean success = blockRegions.remove(pos, region);
		blockAttachments.remove(region, pos);
		if (!blockAttachments.containsKey(region)) {
			addToGlobalDeny(region);
		}
		sync(() -> new RegionSyncRequest.DetachBlock(getName(region), pos));
		return success;
	}
	
	public int detachAll(RenderRegion region) {
		return detachAll(region, true);
	}
	
	private int detachAll(RenderRegion region, boolean addToGlobalDeny) {
		if (region == null) return 0;
		int count = 0;
		for (UUID u : entityAttachments.removeAll(region)) {
			entityRegions.remove(u, region);
			count++;
		}
		LongIterator iter = LongIterators.asLongIterator(blockAttachments.removeAll(region).iterator());
		while (iter.hasNext()) {
			blockRegions.remove(iter.nextLong(), region);
			count++;
		}
		if (addToGlobalDeny) {
			addToGlobalDeny(region);
		}
		sync(() -> new RegionSyncRequest.DetachAll(getName(region)));
		return count;
	}
	
	public BiMap<String, RenderRegion> getRegionsByName() {
		return regionsByName;
	}

	public RenderRegion getByName(String name) {
		return regionsByName.get(name);
	}
	
	public String getName(RenderRegion region) {
		return regionsByName.inverse().get(region);
	}
	
	public SetMultimap<RenderRegion, Long> getAllBlockAttachments() {
		return blockAttachments;
	}
	
	public SetMultimap<RenderRegion, UUID> getAllEntityAttachments() {
		return entityAttachments;
	}
	
	public Set<Long> getBlockAttachments(RenderRegion region) {
		return blockAttachments.get(region);
	}
	
	public Set<UUID> getEntityAttachments(RenderRegion region) {
		return entityAttachments.get(region);
	}
	
	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, BlockEntity be) {
		return shouldRender(blockRegions.get(be.getPos().asLong()), viewerX, viewerY, viewerZ);
	}
	
	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, Entity e) {
		if (e instanceof PlayerEntity) return true;
		return shouldRender(entityRegions.get(e.getUuid()), viewerX, viewerY, viewerZ);
	}

	private boolean shouldRender(Iterable<RenderRegion> assignedRegions, double viewerX, double viewerY, double viewerZ) {
		boolean anyExclusive = false;
		for (var rr : assignedRegions) {
			if (rr.mode() == Mode.EXCLUSIVE) {
				anyExclusive = true;
				if (rr.contains(viewerX, viewerY, viewerZ)) {
					return true;
				}
			} else if (rr.contains(viewerX, viewerY, viewerZ)) {
				return rr.mode() == Mode.ALLOW;
			}
		}
		if (anyExclusive) return false;
		return shouldRender(viewerX, viewerY, viewerZ);
	}
	
	private long lastPos = Long.MIN_VALUE;
	private boolean lastShouldRender = true;

	private boolean shouldRender(double viewerX, double viewerY, double viewerZ) {
		int vX = (int)viewerX;
		int vY = (int)viewerY;
		int vZ = (int)viewerZ;
		long pos = BlockPos.asLong(vX, vY, vZ);
		if (pos == lastPos) {
			return lastShouldRender;
		}
		lastPos = pos;
		long chunkSect = ChunkSectionPos.asLong(vX>>4, vY>>4, vZ>>4);
		for (var rr : denyRegionsByChunkSection.get(chunkSect)) {
			if (rr.contains(viewerX, viewerY, viewerZ)) {
				return lastShouldRender = false;
			}
		}
		return lastShouldRender = true;
	}
	
}
