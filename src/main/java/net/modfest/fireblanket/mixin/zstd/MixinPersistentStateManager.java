package net.modfest.fireblanket.mixin.zstd;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
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
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.modfest.fireblanket.mixinsupport.ZestySupport;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

@Mixin(SavedDataStorage.class)
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
	private Path getDataFile(Identifier id) {
		throw new AbstractMethodError();
	}

	@Shadow
	public abstract CompoundTag readTagFromDisk(
		Path file,
		DataFixTypes dataFixTypes,
		int currentSaveVersion
	) throws IOException;

	@Unique
	private Path getZstdFile(Identifier id) {
		Path path = id.withSuffix(".zat").resolveAgainst(this.dataFolder);
		// Vanilla does this; tho we could potentially disable this if we're feeling daring.
		if (!path.toAbsolutePath().startsWith(this.dataFolder.toAbsolutePath())) {
			throw new IllegalArgumentException("SavedDataStorage attempted file access outside of directory data: " + path);
		}
		return path;
	}

	/**
	 * @author Una
	 * @reason Don't check file before calling readNbt
	 */
	@Overwrite
	private <T extends SavedData> T readSavedData(SavedDataType<T> type) {
		try {
			CompoundTag cmp = this.readTagFromDisk(
				this.getDataFile(type.id()),
				type.dataFixType(),
				SharedConstants.getCurrentVersion().dataVersion().version()
			);
			if (cmp == null) {
				return null;
			}
			RegistryOps<Tag> registryOps = this.registries.createSerializationContext(NbtOps.INSTANCE);
			return type.codec()
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
	@WrapMethod(method = "readTagFromDisk")
	public CompoundTag fireblanket$readNbt(
		Path vanilla,
		DataFixTypes dataFixTypes,
		int dataVersion,
		Operation<CompoundTag> original
	) throws IOException {
		// TODO: this uses an unconditional head cancel because Fabric API wants to mix into the same spot, and has LVT errors when encountering our method.
		InputStream in;
		Path zstd = ZestySupport.zestifyIfVanilla(vanilla);
		if (zstd != null && Files.isRegularFile(zstd)) {
			in = new FastBufferedInputStream(new ZstdInputStream(Files.newInputStream(zstd)));
		} else if (Files.isRegularFile(vanilla)) {
			in = new FastBufferedInputStream(new GZIPInputStream(Files.newInputStream(vanilla)));
		} else {
			// Have fun?
			return null; //original.call(vanilla, dataFixTypes, dataVersion);
		}

		try (in) {
			DataInputStream dis = new DataInputStream(in);
			CompoundTag nbt = NbtIo.read(dis);
			int version = NbtUtils.getDataVersion(nbt, 1343);
			return dataFixTypes == null ? nbt : dataFixTypes.update(fixerUpper, nbt, version, dataVersion);
		}
	}


	@WrapOperation(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/nio/file/Path;)V"
		), method = "tryWrite"
	)
	public void fireblanket$writeZstd(CompoundTag nbt, Path vanilla, Operation<Void> original) throws IOException {
		Path zstd = ZestySupport.zestifyIfVanilla(vanilla);
		if (zstd != null) {
			try (ZstdOutputStream z = new ZstdOutputStream(Files.newOutputStream(zstd))) {
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
