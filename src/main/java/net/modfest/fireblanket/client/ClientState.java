package net.modfest.fireblanket.client;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashSet;
import java.util.Set;

public final class ClientState {
	// Block entities that are set to render a mask around them for identification
	public static final Set<BlockEntityType<?>> MASKED_BERS = new HashSet<>();
	// entities that are set to render a mask around them for identification
	public static final Set<EntityType<?>> MASKED_ENTITIES = new HashSet<>();

	public static boolean displayingEntityTickTimes;
	public static boolean displayingBlockTickTimes;

	public static boolean useRegionRenderer = true;
}
