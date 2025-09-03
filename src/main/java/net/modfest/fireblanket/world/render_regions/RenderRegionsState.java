package net.modfest.fireblanket.world.render_regions;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.Map;

public class RenderRegionsState extends PersistentState {

	private final RenderRegions regions;

	public RenderRegionsState(ServerWorld world) {
		this.regions = new RenderRegions(this::markDirty, req -> {
			for (var player : world.getPlayers()) {
				ServerPlayNetworking.send(player, req);
			}
		});
	}

	public static RenderRegionsState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(
			new PersistentStateType<>(
				"fireblanket_render_regions",
				() -> new RenderRegionsState(world),
				RenderRegions.CODEC
					.xmap(new RenderRegionsState(world)::loadData, state -> state.regions.toData()),
				// Fabric API handles null datafix types
				null
			)
		);
	}

	private RenderRegionsState loadData(final Map<String, RenderRegions.RegionData> data) {
		this.regions.fromData(data);
		this.markDirty();

		return this;
	}

	public RenderRegions getRegions() {
		return regions;
	}
}
