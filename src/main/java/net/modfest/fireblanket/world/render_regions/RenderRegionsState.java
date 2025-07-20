package net.modfest.fireblanket.world.render_regions;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

public class RenderRegionsState extends PersistentState {
//
//	private final RenderRegions regions;
//
//	public RenderRegionsState(ServerWorld world) {
//		this.regions = new RenderRegions(this::markDirty, req -> {
//			for (var player : world.getPlayers()) {
//				ServerPlayNetworking.send(player, req);
//			}
//		});
//	}
//
//	public static RenderRegionsState get(ServerWorld world) {
//		return world.getPersistentStateManager().getOrCreate(
//			new PersistentStateType<>(
//				() -> new RenderRegionsState(world),
//				(nbt, w) -> readNbt(world, nbt),
//				// Fabric API handles null datafix types
//				null
//			),
//			"fireblanket_render_regions");
//	}
//
//	public RenderRegions getRegions() {
//		return regions;
//	}
//
//	public static RenderRegionsState readNbt(ServerWorld world, NbtCompound tag) {
//		RenderRegionsState ret = new RenderRegionsState(world);
//		ret.regions.readNbt(tag);
//		return ret;
//	}
//
//	@Override
//	public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup wrapper) {
//		regions.writeNbt(tag);
//		return tag;
//	}

}
