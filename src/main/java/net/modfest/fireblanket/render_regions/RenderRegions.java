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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.modfest.fireblanket.render_regions.RenderRegion.Mode;

public class RenderRegions {

	private final BiMap<String, RenderRegion> regionsByName = HashBiMap.create();
	private final Long2ReferenceMultimap<RenderRegion> denyRegionsByChunkSection = new Long2ReferenceMultimap<>();
	
	private final Long2ReferenceMultimap<RenderRegion> beRegions = new Long2ReferenceMultimap<>();
	private final ListMultimap<UUID, RenderRegion> entityRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	
	private final SetMultimap<RenderRegion, UUID> entityAttachments = Multimaps.newSetMultimap(new Reference2ReferenceOpenHashMap<>(), ObjectOpenHashSet::new);
	private final SetMultimap<RenderRegion, Long> beAttachments = Multimaps.newSetMultimap(new Reference2ReferenceOpenHashMap<>(), LongOpenHashSet::new);
	
	private final Runnable dirtyListener;
	private final Consumer<RegionSyncCommand> syncer;

	public RenderRegions() {
		this(null, null);
	}

	public RenderRegions(Runnable dirtyListener, Consumer<RegionSyncCommand> syncer) {
		this.dirtyListener = dirtyListener;
		this.syncer = syncer;
	}
	
	public void markDirty() {
		if (dirtyListener != null) dirtyListener.run();
	}
	
	public void sync(Supplier<RegionSyncCommand> supplier) {
		if (syncer != null) {
			var cmd = supplier.get();
			if (!cmd.valid()) return;
			syncer.accept(cmd);
		}
	}

	public void addRegion(String name, RenderRegion region) {
		if (region == null) throw new IllegalArgumentException("region cannot be null");
		RenderRegion old = regionsByName.put(name, region);
		if (old != null) {
			removeRegion(old);
		}
		addRegionToGlobalDeny(region);
		markDirty();
		sync(() -> new RegionSyncCommand.AddRegion(name, region));
	}
	
	public void removeRegion(RenderRegion region) {
		if (region == null) return;
		String name = regionsByName.inverse().remove(region);
		removeRegionFromGlobalDeny(region);
		detachAll(region, false);
		markDirty();
		if (name != null) {
			syncer.accept(new RegionSyncCommand.DestroyRegion(name));
		}
	}
	
	private void removeRegionFromGlobalDeny(RenderRegion region) {
		if (region.mode() != Mode.DENY) return;
		region.affectedChunkLongs().forEach(l -> {
			denyRegionsByChunkSection.remove(l, region);
		});
	}
	
	private void addRegionToGlobalDeny(RenderRegion region) {
		if (region.mode() != Mode.DENY) return;
		region.affectedChunkLongs().forEach(l -> {
			denyRegionsByChunkSection.put(l, region);
		});
	}

	public void clear() {
		regionsByName.clear();
		denyRegionsByChunkSection.clear();
		beRegions.clear();
		entityRegions.clear();
		entityAttachments.clear();
		beAttachments.clear();
		lastPos = Long.MIN_VALUE;
		lastShouldRender = true;
		markDirty();
		sync(() -> new RegionSyncCommand.Reset(true));
	}
	
	public void attachEntity(RenderRegion region, Entity e) {
		attachEntity(region, e.getUuid());
	}
	
	public void attachEntity(RenderRegion region, UUID id) {
		if (region == null) return;
		if (!entityAttachments.containsKey(region)) {
			removeRegionFromGlobalDeny(region);
		}
		entityRegions.put(id, region);
		entityAttachments.put(region, id);
		markDirty();
		sync(() -> new RegionSyncCommand.AttachEntity(getName(region), id));
	}
	
	public void attachBlockEntity(RenderRegion region, BlockEntity be) {
		attachBlockEntity(region, be.getPos().asLong());
	}
	
	public void attachBlockEntity(RenderRegion region, long pos) {
		if (region == null) return;
		if (!beAttachments.containsKey(region)) {
			removeRegionFromGlobalDeny(region);
		}
		beRegions.put(pos, region);
		beAttachments.put(region, pos);
		markDirty();
		sync(() -> new RegionSyncCommand.AttachBlock(getName(region), pos));
	}
	
	public boolean detachEntity(RenderRegion region, Entity e) {
		return detachEntity(region, e.getUuid());
	}
	
	public boolean detachEntity(RenderRegion region, UUID id) {
		if (region == null) return false;
		boolean success = entityRegions.remove(id, region);
		entityAttachments.remove(region, id);
		if (!entityAttachments.containsKey(region)) {
			addRegionToGlobalDeny(region);
		}
		sync(() -> new RegionSyncCommand.DetachEntity(getName(region), id));
		return success;
	}
	
	public boolean detachBlockEntity(RenderRegion region, BlockEntity be) {
		return detachBlockEntity(region, be.getPos().asLong());
	}
	
	public boolean detachBlockEntity(RenderRegion region, long pos) {
		if (region == null) return false;
		boolean success = beRegions.remove(pos, region);
		beAttachments.remove(region, pos);
		if (!beAttachments.containsKey(region)) {
			addRegionToGlobalDeny(region);
		}
		sync(() -> new RegionSyncCommand.DetachBlock(getName(region), pos));
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
		LongIterator iter = LongIterators.asLongIterator(beAttachments.removeAll(region).iterator());
		while (iter.hasNext()) {
			beRegions.remove(iter.nextLong(), region);
			count++;
		}
		if (addToGlobalDeny) {
			addRegionToGlobalDeny(region);
		}
		sync(() -> new RegionSyncCommand.DetachAll(getName(region)));
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
	
	public SetMultimap<RenderRegion, Long> getAllBlockEntityAttachments() {
		return beAttachments;
	}
	
	public SetMultimap<RenderRegion, UUID> getAllEntityAttachments() {
		return entityAttachments;
	}
	
	public Set<Long> getBlockEntityAttachments(RenderRegion region) {
		return beAttachments.get(region);
	}
	
	public Set<UUID> getEntityAttachments(RenderRegion region) {
		return entityAttachments.get(region);
	}
	
	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, BlockEntity be) {
		BlockPos bp = be.getPos();
		long pos = bp.asLong();
		for (var rr : beRegions.get(pos)) {
			if (rr.contains(viewerX, viewerY, viewerZ)) {
				return rr.mode() == Mode.ALLOW;
			}
		}
		return shouldRender(viewerX, viewerY, viewerZ);
	}
	
	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, Entity e) {
		UUID id = e.getUuid();
		for (var rr : entityRegions.get(id)) {
			if (rr.contains(viewerX, viewerY, viewerZ)) {
				return rr.mode() == Mode.ALLOW;
			}
		}
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
