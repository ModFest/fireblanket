package net.modfest.fireblanket.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtType;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(NbtCompound.class)
public abstract class MixinNbtCompound {
	@Mutable
	@Shadow @Final private Map<String, NbtElement> entries;

	@Shadow protected abstract CrashReport createCrashReport(String key, NbtType<?> reader, ClassCastException exception);

	@Shadow @Final public static NbtType<NbtCompound> TYPE;

	@Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
	private void fireblanket$betterMap(Map entries, CallbackInfo ci) {
		this.entries = new Object2ObjectOpenHashMap<>(entries);
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public byte getByte(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.NUMBER_TYPE)) {
				return ((AbstractNbtNumber)nbt).byteValue();
			}
		} catch (ClassCastException var3) {
		}

		return 0;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public short getShort(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.NUMBER_TYPE)) {
				return ((AbstractNbtNumber)nbt).shortValue();
			}
		} catch (ClassCastException var3) {
		}

		return 0;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public int getInt(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.NUMBER_TYPE)) {
				return ((AbstractNbtNumber)nbt).intValue();
			}
		} catch (ClassCastException var3) {
		}

		return 0;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public long getLong(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.NUMBER_TYPE)) {
				return ((AbstractNbtNumber)nbt).longValue();
			}
		} catch (ClassCastException var3) {
		}

		return 0L;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public float getFloat(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.NUMBER_TYPE)) {
				return ((AbstractNbtNumber)nbt).floatValue();
			}
		} catch (ClassCastException var3) {
		}

		return 0.0F;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public double getDouble(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.NUMBER_TYPE)) {
				return ((AbstractNbtNumber)nbt).doubleValue();
			}
		} catch (ClassCastException var3) {
		}

		return 0.0;
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public String getString(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.STRING_TYPE)) {
				return ((NbtElement)nbt).asString();
			}
		} catch (ClassCastException var3) {
		}

		return "";
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public byte[] getByteArray(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.BYTE_ARRAY_TYPE)) {
				return ((NbtByteArray)nbt).getByteArray();
			}
		} catch (ClassCastException var3) {
			throw new CrashException(this.createCrashReport(key, NbtByteArray.TYPE, var3));
		}

		return new byte[0];
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public int[] getIntArray(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.INT_ARRAY_TYPE)) {
				return ((NbtIntArray)nbt).getIntArray();
			}
		} catch (ClassCastException var3) {
			throw new CrashException(this.createCrashReport(key, NbtIntArray.TYPE, var3));
		}

		return new int[0];
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public long[] getLongArray(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.LONG_ARRAY_TYPE)) {
				return ((NbtLongArray)nbt).getLongArray();
			}
		} catch (ClassCastException var3) {
			throw new CrashException(this.createCrashReport(key, NbtLongArray.TYPE, var3));
		}

		return new long[0];
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public NbtCompound getCompound(String key) {
		try {
			NbtElement nbt = this.entries.get(key);
			if (contains(nbt, NbtElement.COMPOUND_TYPE)) {
				return (NbtCompound) nbt;
			}
		} catch (ClassCastException var3) {
			throw new CrashException(this.createCrashReport(key, TYPE, var3));
		}

		return new NbtCompound();
	}

	/**
	 * @author Jasmine
	 *
	 * @reason Remove unnecessary map lookup
	 */
	@Overwrite
	public NbtList getList(String key, int type) {
		try {
			NbtElement nbt = this.entries.get(key);

			if (nbt != null && nbt.getType() == NbtElement.LIST_TYPE) {
				NbtList nbtList = (NbtList)nbt;

				if (!nbtList.isEmpty() && nbtList.getHeldType() != type) {
					return new NbtList();
				}

				return nbtList;
			}
		} catch (ClassCastException var4) {
			throw new CrashException(this.createCrashReport(key, NbtList.TYPE, var4));
		}

		return new NbtList();
	}

	private static boolean contains(NbtElement nbt, int type) {
		int i = nbt == null ? NbtElement.END_TYPE : nbt.getType();

		if (i == type) {
			return true;
		} else if (type != NbtElement.NUMBER_TYPE) {
			return false;
		} else {
			return i == NbtElement.BYTE_TYPE
					|| i == NbtElement.SHORT_TYPE
					|| i == NbtElement.INT_TYPE
					|| i == NbtElement.LONG_TYPE
					|| i == NbtElement.FLOAT_TYPE
					|| i == NbtElement.DOUBLE_TYPE;
		}
	}
}
