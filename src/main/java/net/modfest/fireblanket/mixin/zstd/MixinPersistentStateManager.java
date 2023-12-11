package net.modfest.fireblanket.mixin.zstd;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import com.github.luben.zstd.ZstdInputStream;
import com.mojang.datafixers.DataFixer;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PersistentStateManager.class)
public abstract class MixinPersistentStateManager {

	@Shadow @Final
	private static Logger LOGGER;
	
	@Shadow @Final
	private File directory;
	@Shadow @Final
	private DataFixer dataFixer;
	@Shadow @Final
	private Map<String, PersistentState> loadedStates;

	@Shadow
	private File getFile(String id) { throw new AbstractMethodError(); }

	@Shadow public abstract NbtCompound readNbt(String id, DataFixTypes dataFixTypes, int currentSaveVersion) throws IOException;

	private File getZstdFile(String id) {
		return new File(directory, id+".zat");
	}
	
	/**
	 * @author Una
	 * @reason Don't check file before calling readNbt
	 */
	@Overwrite
	private <T extends PersistentState> T readFromFile(Function<NbtCompound, T> reader,  DataFixTypes dataFixTypes, String id) {
		try {
			NbtCompound cmp = readNbt(id, dataFixTypes, SharedConstants.getGameVersion().getSaveVersion().getId());
			if (cmp == null) return null;
			return reader.apply(cmp.getCompound("data"));
		} catch (Exception e) {
			LOGGER.error("Error loading saved data: {}", id, e);
		}

		return null;
	}

	/**
	 * @author Una
	 * @reason Zstd support, code cleanup
	 */
	@Inject(method = "readNbt", at = @At("HEAD"), cancellable = true)
	public void fireblanket$readNbt(String id, DataFixTypes dataFixTypes, int dataVersion, CallbackInfoReturnable<NbtCompound> cir) throws IOException {
		// TODO: this uses an unconditional head cancel because Fabric API wants to mix into the same spot, and has LVT errors when encountering our method.
		InputStream in;
		File zstd = getZstdFile(id);
		if (zstd.exists()) {
			in = new FastBufferedInputStream(new ZstdInputStream(new FileInputStream(zstd)));
		} else {
			File vanilla = getFile(id);
			if (vanilla.exists()) {
				in = new FastBufferedInputStream(new GZIPInputStream(new FileInputStream(vanilla)));
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
	
}
