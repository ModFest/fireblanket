package net.modfest.fireblanket.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RepeatingBlockState extends PersistentState {
	private final Set<UUID> uuids = new HashSet<>();


	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		NbtList list = new NbtList();

		for (UUID uuid : this.uuids) {
			list.add(NbtHelper.fromUuid(uuid));
		}

		nbt.put("Ids", list);
		return nbt;
	}

	public static RepeatingBlockState readNbt(NbtCompound nbt) {
		NbtList ids = nbt.getList("Ids", NbtElement.INT_ARRAY_TYPE);
		RepeatingBlockState st = new RepeatingBlockState();

		for (NbtElement id : ids) {
			st.uuids.add(NbtHelper.toUuid(id));
		}

		return st;
	}

	public boolean add(UUID uuid) {
		return this.uuids.add(uuid);
	}

	public static PersistentState.Type<RepeatingBlockState> getType() {
		return new Type<>(RepeatingBlockState::new, RepeatingBlockState::readNbt, null);
	}
}
