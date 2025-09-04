package net.modfest.fireblanket.world.render_regions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.modfest.fireblanket.world.render_regions.RegionSyncRequest.FullState;
import net.modfest.fireblanket.world.render_regions.RenderRegion.Mode;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class RenderRegions {

	public static final Codec<Map<String, RegionData>> CODEC = Codec.unboundedMap(Codec.STRING, RegionData.CODEC.codec());

	private final BiMap<String, ExplainedRenderRegion> regionsByName = HashBiMap.create();
	private final Reference2ReferenceMap<RenderRegion, ExplainedRenderRegion> explaineds = new Reference2ReferenceOpenHashMap<>();
	private final Long2ReferenceMultimap<ExplainedRenderRegion> regionsByChunkSection = new Long2ReferenceMultimap<>();

	private final Long2ReferenceMultimap<ExplainedRenderRegion> blockRegions = new Long2ReferenceMultimap<>();
	private final ListMultimap<UUID, ExplainedRenderRegion> entityRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	private final ListMultimap<Identifier, ExplainedRenderRegion> exclusiveEntityTypeRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);
	private final ListMultimap<Identifier, ExplainedRenderRegion> exclusiveBeTypeRegions = Multimaps.newListMultimap(new Object2ReferenceOpenHashMap<>(), ReferenceArrayList::new);

	// These are likely to be so rare to not be worthwhile to try to shove it into the regular exclusive map.
	private final Set<ExplainedRenderRegion> unboundedInvertedExclusiveEntityTypeRegions = new ReferenceOpenHashSet<>();
	private final Set<ExplainedRenderRegion> unboundedInvertedExclusiveBeTypeRegions = new ReferenceOpenHashSet<>();

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
		if (region.mode() == Mode.EXCLUSIVE && !ex.entityTypeBoxBounded && !ex.entityTypeAttachmentsInverted) {
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
		if (region.mode() == Mode.EXCLUSIVE && !ex.beTypeBoxBounded && !ex.beTypeAttachmentsInverted) {
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

	void applyMeta(ExplainedRenderRegion ex) {
		final RenderRegion r = this.getByName(ex.name);
		final ExplainedRenderRegion nex = this.explaineds.get(r);

		onUpdateMeta(nex,
			nex.entityTypeBoxBounded,
			ex.entityTypeBoxBounded,
			nex.entityTypeAttachmentsInverted,
			ex.entityTypeAttachmentsInverted,
			nex.entityTypeAttachments,
			unboundedInvertedExclusiveEntityTypeRegions,
			exclusiveEntityTypeRegions
		);

		onUpdateMeta(nex,
			nex.beTypeBoxBounded,
			ex.beTypeBoxBounded,
			nex.beTypeAttachmentsInverted,
			ex.beTypeAttachmentsInverted,
			nex.beTypeAttachments,
			unboundedInvertedExclusiveBeTypeRegions,
			exclusiveBeTypeRegions
		);

		nex.copyMeta(ex);
	}

	private void metaCommon(ExplainedRenderRegion ex) {
		this.markDirty();
		this.sync(() -> new RegionSyncRequest.UpdateRegionMetadata(ex.name, ex.getMeta()));
	}

	public void setEntityTypeAttachmentsInverted(RenderRegion region, boolean bool) {
		if (region == null) {
			return;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		onInvertExclusion(ex,
			ex.entityTypeAttachmentsInverted,
			bool,
			unboundedInvertedExclusiveEntityTypeRegions,
			ex.entityTypeBoxBounded ? Set.of() : ex.entityTypeAttachments,
			exclusiveEntityTypeRegions
		);
		ex.entityTypeAttachmentsInverted = bool;
		this.metaCommon(ex);
	}

	public boolean getEntityTypeAttachmentsInverted(RenderRegion region) {
		if (region == null) {
			return false;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		return ex.entityTypeAttachmentsInverted;
	}

	public void setEntityTypeBoxBounded(RenderRegion region, boolean bool) {
		if (region == null) {
			return;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		if (!ex.entityTypeAttachmentsInverted) {
			onUnboundExclusion(ex, ex.entityTypeBoxBounded, bool, ex.entityTypeAttachments, exclusiveEntityTypeRegions);
		}
		ex.entityTypeBoxBounded = bool;
		this.metaCommon(ex);
	}

	public boolean getEntityTypeBoxBounded(RenderRegion region) {
		if (region == null) {
			return false;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		return ex.entityTypeBoxBounded;
	}

	public void setBeTypeAttachmentsInverted(RenderRegion region, boolean bool) {
		if (region == null) {
			return;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		onInvertExclusion(ex,
			ex.beTypeAttachmentsInverted,
			bool,
			unboundedInvertedExclusiveBeTypeRegions,
			ex.beTypeBoxBounded ? Set.of() : ex.beTypeAttachments,
			exclusiveBeTypeRegions
		);
		ex.beTypeAttachmentsInverted = bool;
		this.metaCommon(ex);
	}

	public boolean getBeTypeAttachmentsInverted(RenderRegion region) {
		if (region == null) {
			return false;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		return ex.beTypeAttachmentsInverted;
	}

	public void setBeTypeBoxBounded(RenderRegion region, boolean bool) {
		if (region == null) {
			return;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		if (!ex.beTypeAttachmentsInverted) {
			onUnboundExclusion(ex, ex.beTypeBoxBounded, bool, ex.beTypeAttachments, exclusiveBeTypeRegions);
		}
		ex.beTypeBoxBounded = bool;
		this.metaCommon(ex);
	}

	public boolean getBeTypeBoxBounded(RenderRegion region) {
		if (region == null) {
			return false;
		}
		ExplainedRenderRegion ex = explaineds.get(region);
		return ex.beTypeBoxBounded;
	}

	private static void onUpdateMeta(
		final ExplainedRenderRegion ex,
		final boolean oldBounded,
		final boolean newBounded,
		final boolean oldInverted,
		final boolean newInverted,
		final Set<Identifier> ids,
		final Set<ExplainedRenderRegion> unboundedInversions,
		final Multimap<Identifier, ExplainedRenderRegion> exclusives
	) {
		// Non-exclusive regions don't need this.
		if (ex.reg.mode() != Mode.EXCLUSIVE) {
			return;
		}

		if (oldInverted != newInverted) {
			if (newInverted) {
				for (var id : ids) {
					exclusives.remove(id, ex);
				}
				if (!newBounded) {
					unboundedInversions.add(ex);
				}
			} else {
				unboundedInversions.remove(ex);
				if (newBounded) {
					for (var id : ids) {
						exclusives.put(id, ex);
					}
				}
			}
			return;
		}

		if (newInverted) {
			return;
		}

		onUnboundExclusion(ex, oldBounded, newBounded, ids, exclusives);
	}

	private static void onInvertExclusion(
		final ExplainedRenderRegion ex,
		final boolean oldBool,
		final boolean newBool,
		final Set<ExplainedRenderRegion> regions,
		final Set<Identifier> ids,
		final Multimap<Identifier, ExplainedRenderRegion> exclusives
	) {
		if ((oldBool == newBool) || ex.reg.mode() != Mode.EXCLUSIVE) {
			return;
		}

		if (newBool) {
			regions.add(ex);
			for (var id : ids) {
				exclusives.remove(id, ex);
			}
		} else {
			regions.remove(ex);
			for (var id : ids) {
				exclusives.put(id, ex);
			}
		}
	}

	private static void onUnboundExclusion(
		final ExplainedRenderRegion ex,
		final boolean oldBool,
		final boolean newBool,
		Set<Identifier> ids,
		final Multimap<Identifier, ExplainedRenderRegion> exclusives
	) {
		if ((oldBool == newBool) || ex.reg.mode() != Mode.EXCLUSIVE) {
			return;
		}

		if (newBool) {
			for (var id : ids) {
				exclusives.remove(id, ex);
			}
		} else {
			for (var id : ids) {
				exclusives.put(id, ex);
			}
		}
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
		long targetPos = be.getPos().asLong();
		long viewerPos = BlockPos.asLong((int) viewerX, (int) viewerY, (int) viewerZ);
		if (be instanceof RegionSubject rs) {
			Boolean cached = rs.fireblanket$cachedShouldRender(era, viewerPos, targetPos);
			if (cached != null) return cached;
		}
		boolean res = shouldRender(
			ExplainedRenderRegion::isBlockEntityTypeTargeted,
			BlockEntityType.getId(be.getType()),
			exclusiveBeTypeRegions,
			unboundedInvertedExclusiveBeTypeRegions,
			blockRegions.get(be.getPos().asLong()),
			be, ChunkSectionPos.toLong(be.getPos()),
			viewerX, viewerY, viewerZ
		);
		if (be instanceof RegionSubject rs) {
			rs.fireblanket$setCachedState(era, viewerPos, targetPos, res);
		}
		return res;
	}

	public boolean shouldRender(double viewerX, double viewerY, double viewerZ, Entity e) {
		if (e instanceof PlayerEntity && e.shouldRenderName()) return true;
		long targetPos = e.getBlockPos().asLong();
		long viewerPos = BlockPos.asLong((int) viewerX, (int) viewerY, (int) viewerZ);
		if (e instanceof RegionSubject rs) {
			Boolean cached = rs.fireblanket$cachedShouldRender(era, viewerPos, targetPos);
			if (cached != null) return cached;
		}
		boolean res = shouldRender(
			ExplainedRenderRegion::isEntityTypeTargeted,
			EntityType.getId(e.getType()),
			exclusiveEntityTypeRegions,
			unboundedInvertedExclusiveEntityTypeRegions,
			entityRegions.get(e.getUuid()),
			e, ChunkSectionPos.toLong(e.getBlockPos()),
			viewerX, viewerY, viewerZ
		);
		if (e instanceof RegionSubject rs) {
			rs.fireblanket$setCachedState(era, viewerPos, targetPos, res);
		}
		return res;
	}

	private <T> boolean shouldRender(
		BiPredicate<ExplainedRenderRegion, T> targets, Identifier type,
		ListMultimap<Identifier, ExplainedRenderRegion> exclusiveTypeRegions,
		Iterable<ExplainedRenderRegion> unboundedInvertedExclusives,
		Iterable<ExplainedRenderRegion> assignedRegions, T target,
		long targetChunkSect,
		double viewerX, double viewerY, double viewerZ
	) {
		boolean anyExclusive = false;
		int vX = (int) viewerX;
		int vY = (int) viewerY;
		int vZ = (int) viewerZ;
		long chunkSect = ChunkSectionPos.asLong(vX >> 4, vY >> 4, vZ >> 4);

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
			if (rr.reg.mode() == Mode.EXCLUSIVE && (containsCache = targets.test(rr, target))) {
				anyExclusive = true;
			}
			if (rr.reg.contains(viewerX, viewerY, viewerZ)) {
				if (containsCache == null ? targets.test(rr, target) : containsCache) {
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

		// Lightly more efficient bounded exclusive regions
		// This check should be infallible.
		// It is safe to assume that any fails here is allowed.
		for (var rr : regionsByChunkSection.get(targetChunkSect)) {
			if (rr.reg.mode() != Mode.EXCLUSIVE) {
				continue;
			}
			if (targets.test(rr, target)) {
				return false;
			}
		}

		// This is going to be pretty rare and usually accidental.
		for (var rr : unboundedInvertedExclusives) {
			if (targets.test(rr, target) && !rr.reg.contains(viewerX, viewerY, viewerZ)) {
				return false;
			}
		}

		// exclusive regions not matched above must mean reject
		for (var rr : exclusiveTypeRegions.get(type)) {
			if (!rr.reg.contains(viewerX, viewerY, viewerZ)) {
				return false;
			}
		}
		if (anyExclusive) return false;
		return !blanketDeny;
	}

	public RenderRegions fromData(final Map<String, RegionData> data) {
		data.forEach((name, d) -> {
			RenderRegion r = d.region;
			add(name, r);

			for (UUID entity : d.entities) {
				attachEntity(r, entity);
			}

			for (long block : d.blocks) {
				attachBlock(r, block);
			}

			for (Identifier id : d.entityTypes) {
				attachEntityType(r, id);
			}

			for (Identifier id : d.blockTypes) {
				attachBlockEntityType(r, id);
			}

			var ex = explaineds.get(r);

			ex.applyMeta(d.meta);
		});

		return this;
	}

	public Map<String, RegionData> toData() {
		final Map<String, RegionData> map = new HashMap<>();

		for (var ex : explaineds.values()) {
			map.put(ex.name, new RegionData(
				ex.reg,
				ex.entityAttachments,
				ex.blockAttachments,
				ex.entityTypeAttachments,
				ex.beTypeAttachments,
				ex.getMeta()
			));
		}

		return map;
	}

	public record RegionData(
		RenderRegion region,
		Set<UUID> entities,
		LongSet blocks,
		Set<Identifier> entityTypes,
		Set<Identifier> blockTypes,
		BitSet meta
	) {
		private static final Codec<Set<Identifier>> ID_SET_CODEC = Identifier.CODEC
			.listOf()
			.xmap(HashSet::new, List::copyOf);

		public static final MapCodec<RegionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			RenderRegion.CODEC.forGetter(RegionData::region),
			Codec.INT_STREAM
				.xmap(RegionData::deser, RegionData::ser)
				.fieldOf("EAtt")
				.orElseGet(HashSet::new)
				.forGetter(RegionData::entities),
			Codec.LONG_STREAM
				.xmap(RegionData::toSet, LongSet::longStream)
				.fieldOf("BEAtt")
				.orElseGet(LongOpenHashSet::new)
				.forGetter(RegionData::blocks),
			ID_SET_CODEC
				.fieldOf("ETAtt")
				.orElseGet(HashSet::new)
				.forGetter(RegionData::entityTypes),
			ID_SET_CODEC
				.fieldOf("BETAtt")
				.orElseGet(HashSet::new)
				.forGetter(RegionData::blockTypes),
			Codecs.BIT_SET
				.fieldOf("Meta")
				.orElseGet(BitSet::new)
				.forGetter(RegionData::meta)
		).apply(instance, RegionData::new));

		private static Set<UUID> deser(final IntStream stream) {
			final int[] array = stream.toArray();
			final HashSet<UUID> set = new HashSet<>();

			for (int i = 0; i < array.length; i += 4) {
				set.add(Uuids.toUuid(Arrays.copyOfRange(array, i, i + 4)));
			}

			return set;
		}

		private static IntStream ser(final Set<UUID> set) {
			final IntList list = new IntArrayList();

			for (var uuid : set) {
				list.addAll(IntList.of(Uuids.toIntArray(uuid)));
			}

			return list.intStream();
		}

		private static LongSet toSet(final LongStream stream) {
			return stream.collect(LongOpenHashSet::new, LongOpenHashSet::add, LongOpenHashSet::addAll);
		}
	}

}
