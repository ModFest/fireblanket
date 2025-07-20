package net.modfest.fireblanket.mixin.zstd;

import com.github.luben.zstd.ZstdInputStream;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

@Mixin(PersistentStateManager.class)
public abstract class MixinPersistentStateManager {
//
//	@Shadow
//	@Final
//	private static Logger LOGGER;
//
//	@Shadow
//	@Final
//	private File directory;
//	@Shadow
//	@Final
//	private DataFixer dataFixer;
//	@Final
//	private RegistryWrapper.WrapperLookup registries;
//	@Final
//	private PersistentState.Context context;
//
//	@Shadow
//	private File getFile(String id) {
//		throw new AbstractMethodError();
//	}
//
//	@Shadow
//	public abstract NbtCompound readNbt(String id, DataFixTypes dataFixTypes, int currentSaveVersion) throws IOException;
//
//
//	@Unique
//	private File getZstdFile(String id) {
//		return new File(directory, id + ".zat");
//	}
//
//	/**
//	 * @return
//	 * @author Una
//	 * @reason Don't check file before calling readNbt
//	 */
////	@Overwrite
////	private <T extends PersistentState> T readFromFile(PersistentStateType<T> type) {
////		try {
////			NbtCompound cmp = readNbt(type.id(), type.dataFixType(), SharedConstants.getGameVersion().dataVersion().id());
////			if (cmp == null) return null;
////			RegistryOps<NbtElement> registryOps = this.registries.getOps(NbtOps.INSTANCE);
////			return (T)((Codec)type.codec().apply(this.context))
////				.parse(registryOps, nbtCompound.get("data"))
////				.resultOrPartial(string -> LOGGER.error("Failed to parse saved data for '{}': {}", type, string))
////				.orElse(null);
////			return type.codec().apply(
////				cmp.getCompound("data").orElse(null), this.registryLookup
////			);
////		} catch (Exception e) {
////			LOGGER.error("Error loading saved data: {}", id, e);
////		}
////
////		return null;
////	}
//
//	@WrapOperation(
//		method = "readFromFile", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;exists(Ljava/nio/file/Path;[Ljava/nio/file/LinkOption;)Z")
//	)
//	private boolean skipCheckForFile(Path path, LinkOption[] linkOptions, Operation<Boolean> original){
//		return true;
//	}
//
//	/**
//	 * @author Una
//	 * @reason Zstd support, code cleanup
//	 */
//	@Inject(method = "readNbt", at = @At("HEAD"), cancellable = true)
//	public void fireblanket$readNbt(String id, DataFixTypes dataFixTypes, int dataVersion, CallbackInfoReturnable<NbtCompound> cir) throws IOException {
//		// TODO: this uses an unconditional head cancel because Fabric API wants to mix into the same spot, and has LVT errors when encountering our method.
//		InputStream in;
//		File zstd = getZstdFile(id);
//		if (zstd.exists()) {
//			in = new FastBufferedInputStream(new ZstdInputStream(new FileInputStream(zstd)));
//		} else {
//			File vanilla = getFile(id);
//			if (vanilla.exists()) {
//				in = new FastBufferedInputStream(new GZIPInputStream(new FileInputStream(vanilla)));
//			} else {
//				cir.setReturnValue(null);
//				return;
//			}
//		}
//
//		try (in) {
//			DataInputStream dis = new DataInputStream(in);
//			NbtCompound nbt = NbtIo.readCompound(dis);
//			int version = NbtHelper.getDataVersion(nbt, 1343);
//			cir.setReturnValue(dataFixTypes == null ? nbt : dataFixTypes.update(dataFixer, nbt, version, dataVersion));
//		}
//	}

}
