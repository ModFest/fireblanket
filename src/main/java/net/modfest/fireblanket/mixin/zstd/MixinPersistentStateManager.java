package net.modfest.fireblanket.mixin.zstd;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

@Mixin(DimensionDataStorage.class)
public abstract class MixinPersistentStateManager {

	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	@Final
	private Path dataFolder;
	@Shadow
	@Final
	private DataFixer fixerUpper;
	@Shadow
	@Final
	private HolderLookup.Provider registries;
	@Shadow
	@Final
	private SavedData.Context context;

	@Shadow
	private Path getDataFile(String id) {
		throw new AbstractMethodError();
	}

	@Shadow
	public abstract CompoundTag readTagFromDisk(
		String id,
		DataFixTypes dataFixTypes,
		int currentSaveVersion
	) throws IOException;

	@Unique
	private Path getZstdFile(String id) {
		return dataFolder.resolve(id + ".zat");
	}

	/**
	 * @author Una
	 * @reason Don't check file before calling readNbt
	 */
	@Overwrite
	private <T extends SavedData> T readSavedData(SavedDataType<T> type) {
		try {
			CompoundTag cmp = this.readTagFromDisk(
				type.id(),
				type.dataFixType(),
				SharedConstants.getCurrentVersion().dataVersion().version()
			);
			if (cmp == null) {
				return null;
			}
			RegistryOps<Tag> registryOps = this.registries.createSerializationContext(NbtOps.INSTANCE);
			return type.codec().apply(this.context)
				.parse(registryOps, cmp.get("data"))
				.resultOrPartial(string -> LOGGER.error("Failed to parse saved data for '{}': {}", type, string))
				.orElse(null);
		} catch (Exception e) {
			LOGGER.error("Error loading saved data: {}", type, e);
		}

		return null;
	}

	/**
	 * @author Una
	 * @reason Zstd support, code cleanup
	 */
	@Inject(method = "readTagFromDisk", at = @At("HEAD"), cancellable = true)
	public void fireblanket$readNbt(
		String id,
		DataFixTypes dataFixTypes,
		int dataVersion,
		CallbackInfoReturnable<CompoundTag> cir
	) throws IOException {
		// TODO: this uses an unconditional head cancel because Fabric API wants to mix into the same spot, and has LVT errors when encountering our method.
		InputStream in;
		Path zstd = getZstdFile(id);
		if (Files.isRegularFile(zstd)) {
			in = new FastBufferedInputStream(new ZstdInputStream(Files.newInputStream(zstd)));
		} else {
			Path vanilla = getDataFile(id);
			if (Files.isRegularFile(vanilla)) {
				in = new FastBufferedInputStream(new GZIPInputStream(Files.newInputStream(vanilla)));
			} else {
				cir.setReturnValue(null);
				return;
			}
		}

		try (in) {
			DataInputStream dis = new DataInputStream(in);
			CompoundTag nbt = NbtIo.read(dis);
			int version = NbtUtils.getDataVersion(nbt, 1343);
			cir.setReturnValue(dataFixTypes == null ? nbt : dataFixTypes.update(fixerUpper, nbt, version, dataVersion));
		}
	}


	@WrapOperation(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"
		), method = "tryWrite"
	)
	public void fireblanket$writeZstd(CompoundTag nbt, Path vanilla, Operation<Void> original) throws IOException {
		String path = vanilla.toAbsolutePath().toString();
		if (path.endsWith(".dat")) {
			File zstd = new File(path.substring(0, path.length() - 4) + ".zat");
			try (ZstdOutputStream z = new ZstdOutputStream(new FileOutputStream(zstd))) {
				z.setChecksum(true);
				z.setLevel(4);
				NbtIo.writeUnnamedTagWithFallback(nbt, new DataOutputStream(z));
			}

			Files.deleteIfExists(vanilla);
		} else {
			// oookay, I dunno what you want. have fun.
			original.call(nbt, vanilla);
		}
	}
}
