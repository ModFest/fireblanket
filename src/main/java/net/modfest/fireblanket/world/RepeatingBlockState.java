package net.modfest.fireblanket.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

public class RepeatingBlockState extends PersistentState {
	private final Set<UUID> uuids = new HashSet<>();

	public static Codec<RepeatingBlockState> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			Uuids.SET_CODEC.fieldOf("uuids").forGetter(state -> state.uuids)
		).apply(instance, RepeatingBlockState::new)
	);

	public RepeatingBlockState() {
		this.markDirty();
	}

	public RepeatingBlockState(Set<UUID> uuids) {
		this.uuids.addAll(uuids);
	}

	public boolean add(UUID uuid) {
		return this.uuids.add(uuid);
	}

	public static PersistentStateType<RepeatingBlockState> TYPE = new PersistentStateType<>(
		"fireblanket:repeating_command_block_placed", RepeatingBlockState::new, CODEC, null
	);

	public static PersistentStateType<RepeatingBlockState> getType() {
		return TYPE;
	}
}
