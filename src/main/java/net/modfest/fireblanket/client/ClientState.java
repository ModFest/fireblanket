package net.modfest.fireblanket.client;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientState {
	// Block entities that are set to render a mask around them for identification
	public static final Set<BlockEntityType<?>> MASKED_BERS = new HashSet<>();
	// entities that are set to render a mask around them for identification
	public static final Set<EntityType<?>> MASKED_ENTITIES = new HashSet<>();

	public static boolean wireframe = false;
}
