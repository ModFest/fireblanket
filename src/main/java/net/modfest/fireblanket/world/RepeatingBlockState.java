package net.modfest.fireblanket.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RepeatingBlockState extends SavedData {
	private final Set<UUID> uuids = new HashSet<>();

	public static Codec<RepeatingBlockState> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
			UUIDUtil.CODEC_SET.fieldOf("uuids").forGetter(state -> state.uuids)
		).apply(instance, RepeatingBlockState::new)
	);

	public RepeatingBlockState() {
		this.setDirty();
	}

	public RepeatingBlockState(Set<UUID> uuids) {
		this.uuids.addAll(uuids);
	}

	public boolean add(UUID uuid) {
		return this.uuids.add(uuid);
	}

	public static SavedDataType<RepeatingBlockState> TYPE = new SavedDataType<>(
		"fireblanket:repeating_command_block_placed", RepeatingBlockState::new, CODEC, null
	);

	public static SavedDataType<RepeatingBlockState> getType() {
		return TYPE;
	}
}
