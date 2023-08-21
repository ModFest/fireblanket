package net.modfest.fireblanket.render_regions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.modfest.fireblanket.Fireblanket;

public class RenderRegionsState extends PersistentState {

	private final RenderRegions regions;
	
	public RenderRegionsState(ServerWorld world) {
		this.regions = new RenderRegions(this::markDirty, req -> {
			var pkt = req.toPacket(Fireblanket.REGIONS_UPDATE);
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
		ret.regions.readNbt(tag);
		return ret;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		regions.writeNbt(tag);
		return tag;
	}

}
