package net.modfest.fireblanket.mixin.serde;

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
import java.util.Optional;

@Mixin(NbtCompound.class)
public abstract class MixinNbtCompound {
//	@Mutable
//	@Shadow
//	@Final
//	private Map<String, NbtElement> entries;
//
//	@Shadow
//	protected abstract CrashReport createCrashReport(String key, NbtType<?> reader, ClassCastException exception);
//
//	@Shadow
//	@Final
//	public static NbtType<NbtCompound> TYPE;
//
//	@Inject(method = "<init>(Ljava/util/Map;)V", at = @At("TAIL"))
//	private void fireblanket$betterMap(Map<String, NbtElement> entries, CallbackInfo ci) {
//		this.entries = new Object2ObjectOpenHashMap<>(entries);
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public byte getByte(String key, byte fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.BYTE_TYPE)) {
//				return ((AbstractNbtNumber) nbt).byteValue();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return fallback;
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public short getShort(String key, short fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.SHORT_TYPE)) {
//				return ((AbstractNbtNumber) nbt).shortValue();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return fallback;
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public int getInt(String key, int fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.INT_TYPE)) {
//				return ((AbstractNbtNumber) nbt).intValue();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return fallback;
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public long getLong(String key, long fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.LONG_TYPE)) {
//				return ((AbstractNbtNumber) nbt).longValue();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return fallback;
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public float getFloat(String key, float fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.FLOAT_TYPE)) {
//				return ((AbstractNbtNumber) nbt).floatValue();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return fallback;
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public double getDouble(String key, double fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.DOUBLE_TYPE)) {
//				return ((AbstractNbtNumber) nbt).doubleValue();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return fallback;
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public String getString(String key, String fallback) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.STRING_TYPE)) {
//				return nbt.toString();
//			}
//		} catch (ClassCastException ignored) {
//		}
//
//		return "";
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public Optional<byte[]> getByteArray(String key) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.BYTE_ARRAY_TYPE)) {
//				return Optional.of(((NbtByteArray) nbt).getByteArray());
//			}
//		} catch (ClassCastException var3) {
//			throw new CrashException(this.createCrashReport(key, NbtByteArray.TYPE, var3));
//		}
//
//		return Optional.empty();
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public Optional<int[]> getIntArray(String key) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.INT_ARRAY_TYPE)) {
//				return Optional.of(((NbtIntArray) nbt).getIntArray());
//			}
//		} catch (ClassCastException var3) {
//			throw new CrashException(this.createCrashReport(key, NbtIntArray.TYPE, var3));
//		}
//
//		return Optional.empty();
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public Optional<long[]> getLongArray(String key) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.LONG_ARRAY_TYPE)) {
//				return Optional.of(((NbtLongArray) nbt).getLongArray());
//			}
//		} catch (ClassCastException var3) {
//			throw new CrashException(this.createCrashReport(key, NbtLongArray.TYPE, var3));
//		}
//
//		return Optional.empty();
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public Optional<NbtCompound> getCompound(String key) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//			if (contains(nbt, NbtElement.COMPOUND_TYPE)) {
//				return Optional.of((NbtCompound) nbt);
//			}
//		} catch (ClassCastException var3) {
//			throw new CrashException(this.createCrashReport(key, TYPE, var3));
//		}
//
//		return Optional.empty();
//	}
//
//	/**
//	 * @author Jasmine
//	 * @reason Remove unnecessary map lookup
//	 */
//	@Overwrite
//	public Optional<NbtList> getList(String key) {
//		try {
//			NbtElement nbt = this.entries.get(key);
//
//			if (nbt != null && nbt.getType() == NbtElement.LIST_TYPE) {
//				NbtList nbtList = (NbtList) nbt;
//
//				if (!nbtList.isEmpty() && nbtList.getHeldType() != type) {
//					return new NbtList();
//				}
//
//				return nbtList;
//			}
//		} catch (ClassCastException var4) {
//			throw new CrashException(this.createCrashReport(key, NbtList.TYPE, var4));
//		}
//
//		return new NbtList();
//	}
//
//	private static boolean contains(NbtElement nbt, int type) {
//		int i = nbt == null ? NbtElement.END_TYPE : nbt.getType();
//
//		if (i == type) {
//			return true;
//		} else if (type != NbtElement.NUMBER_TYPE) {
//			return false;
//		} else {
//			return i == NbtElement.BYTE_TYPE
//				|| i == NbtElement.SHORT_TYPE
//				|| i == NbtElement.INT_TYPE
//				|| i == NbtElement.LONG_TYPE
//				|| i == NbtElement.FLOAT_TYPE
//				|| i == NbtElement.DOUBLE_TYPE;
//		}
//	}
}
