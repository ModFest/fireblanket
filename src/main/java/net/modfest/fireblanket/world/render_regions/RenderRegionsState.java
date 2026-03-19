package net.modfest.fireblanket.world.render_regions;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.modfest.fireblanket.FireblanketConstants;

import java.util.Map;

public class RenderRegionsState extends SavedData {

	private final RenderRegions regions;

	public RenderRegionsState(ServerLevel world) {
		this.regions = new RenderRegions(this::setDirty, req -> {
			for (var player : world.players()) {
				ServerPlayNetworking.send(player, req);
			}
		});
	}

	public static RenderRegionsState get(ServerLevel world) {
		return world.getDataStorage().computeIfAbsent(
			new SavedDataType<>(
				FireblanketConstants.id("render_regions"),
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
		this.setDirty();

		return this;
	}

	public RenderRegions getRegions() {
		return regions;
	}
}
