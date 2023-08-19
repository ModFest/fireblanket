package net.modfest.fireblanket.render_regions;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongCollection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.render_regions.RenderRegion.Mode;

public class RenderRegionsState extends PersistentState {

	private final RenderRegions regions;
	
	public RenderRegionsState(ServerWorld world) {
		this.regions = new RenderRegions(this::markDirty, cmd -> {
			var pkt = cmd.toPacket(Fireblanket.REGIONS_UPDATE);
			for (var player : world.getPlayers()) {
				player.networkHandler.sendPacket(pkt);
			}
		});
	}

	public static RenderRegionsState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(
				(nbt) -> readNbt(world, nbt),
				() -> new RenderRegionsState(world),
			"fireblanket_render_regions");
	}
	
	public RenderRegions getRegions() {
		return regions;
	}
	
	public static RenderRegionsState readNbt(ServerWorld world, NbtCompound tag) {
		RenderRegionsState ret = new RenderRegionsState(world);
		for (String k : tag.getKeys()) {
			NbtCompound cmp = tag.getCompound(k);
			Mode m = Mode.valueOf(cmp.getString("Mode"));
			int[] box = cmp.getIntArray("Box");
			RenderRegion r = new RenderRegion(box[0], box[1], box[2], box[3], box[4], box[5], m);
			ret.regions.add(k, r);
			int[] entities = cmp.getIntArray("EAtt");
			for (int i = 0; i < entities.length; i += 4) {
				ret.regions.attachEntity(r, Uuids.toUuid(Arrays.copyOfRange(entities, i, i+4)));
			}
			long[] blockentities = cmp.getLongArray("BEAtt");
			for (long l : blockentities) {
				ret.regions.attachBlock(r, l);
			}
		}
		return ret;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		for (var en : regions.getRegionsByName().entrySet()) {
			RenderRegion r = en.getValue();
			NbtCompound cmp = new NbtCompound();
			cmp.putString("Mode", r.mode().name());
			cmp.putIntArray("Box", new int[] { r.minX(), r.minY(), r.minZ(), r.maxX(), r.maxY(), r.maxZ() });
			IntList entities = new IntArrayList();
			for (var entityId : regions.getEntityAttachments(r)) {
				entities.addAll(IntList.of(Uuids.toIntArray(entityId)));
			}
			cmp.putIntArray("EAtt", entities.toIntArray());
			Set<Long> bea = regions.getBlockAttachments(r);
			if (bea instanceof LongCollection lc) {
				cmp.putLongArray("BEAtt", lc.toLongArray());
			} else {
				cmp.putLongArray("BEAtt", List.copyOf(bea));
			}
			tag.put(en.getKey(), cmp);
		}
		return tag;
	}

}
