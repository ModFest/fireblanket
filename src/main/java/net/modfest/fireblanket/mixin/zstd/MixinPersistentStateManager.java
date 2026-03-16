package net.modfest.fireblanket.mixin.zstd;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
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

@Mixin(PersistentStateManager.class)
public abstract class MixinPersistentStateManager {

	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	@Final
	private Path directory;
	@Shadow
	@Final
	private DataFixer dataFixer;
	@Shadow
	@Final
	private RegistryWrapper.WrapperLookup registries;
	@Shadow
	@Final
	private PersistentState.Context context;

	@Shadow
	private Path getFile(String id) {
		throw new AbstractMethodError();
	}

	@Shadow
	public abstract NbtCompound readNbt(
		String id,
		DataFixTypes dataFixTypes,
		int currentSaveVersion
	) throws IOException;

	@Unique
	private Path getZstdFile(String id) {
		return directory.resolve(id + ".zat");
	}

	/**
	 * @author Una
	 * @reason Don't check file before calling readNbt
	 */
	@Overwrite
	private <T extends PersistentState> T readFromFile(PersistentStateType<T> type) {
		try {
			NbtCompound cmp = this.readNbt(
				type.id(),
				type.dataFixType(),
				SharedConstants.getGameVersion().dataVersion().id()
			);
			if (cmp == null) {
				return null;
			}
			RegistryOps<NbtElement> registryOps = this.registries.getOps(NbtOps.INSTANCE);
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
	@Inject(method = "readNbt", at = @At("HEAD"), cancellable = true)
	public void fireblanket$readNbt(
		String id,
		DataFixTypes dataFixTypes,
		int dataVersion,
		CallbackInfoReturnable<NbtCompound> cir
	) throws IOException {
		// TODO: this uses an unconditional head cancel because Fabric API wants to mix into the same spot, and has LVT errors when encountering our method.
		InputStream in;
		Path zstd = getZstdFile(id);
		if (Files.isRegularFile(zstd)) {
			in = new FastBufferedInputStream(new ZstdInputStream(Files.newInputStream(zstd)));
		} else {
			Path vanilla = getFile(id);
			if (Files.isRegularFile(vanilla)) {
				in = new FastBufferedInputStream(new GZIPInputStream(Files.newInputStream(vanilla)));
			} else {
				cir.setReturnValue(null);
				return;
			}
		}

		try (in) {
			DataInputStream dis = new DataInputStream(in);
			NbtCompound nbt = NbtIo.readCompound(dis);
			int version = NbtHelper.getDataVersion(nbt, 1343);
			cir.setReturnValue(dataFixTypes == null ? nbt : dataFixTypes.update(dataFixer, nbt, version, dataVersion));
		}
	}


	@WrapOperation(
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/nbt/NbtIo.writeCompressed(Lnet/minecraft/nbt/NbtCompound;Ljava/nio/file/Path;)V"
		), method = "save"
	)
	public void fireblanket$writeZstd(NbtCompound nbt, Path vanilla, Operation<Void> original) throws IOException {
		String path = vanilla.toAbsolutePath().toString();
		if (path.endsWith(".dat")) {
			File zstd = new File(path.substring(0, path.length() - 4) + ".zat");
			try (ZstdOutputStream z = new ZstdOutputStream(new FileOutputStream(zstd))) {
				z.setChecksum(true);
				z.setLevel(4);
				NbtIo.write(nbt, new DataOutputStream(z));
			}

			Files.deleteIfExists(vanilla);
		} else {
			// oookay, I dunno what you want. have fun.
			original.call(nbt, vanilla);
		}
	}
}
