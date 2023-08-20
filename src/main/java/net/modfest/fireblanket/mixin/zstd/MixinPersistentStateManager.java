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

@Mixin(PersistentStateManager.class)
public class MixinPersistentStateManager {

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
	
	private File getZstdFile(String id) {
		return new File(directory, id+".zat");
	}
	
	/**
	 * @author Una
	 * @reason Don't check file before calling readNbt
	 */
	@Overwrite
	private <T extends PersistentState> T readFromFile(Function<NbtCompound, T> reader, String id) {
		try {
			NbtCompound cmp = readNbt(id, SharedConstants.getGameVersion().getSaveVersion().getId());
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
	@Overwrite
	public NbtCompound readNbt(String id, int dataVersion) throws IOException {
		InputStream in;
		File zstd = getZstdFile(id);
		if (zstd.exists()) {
			in = new FastBufferedInputStream(new ZstdInputStream(new FileInputStream(zstd)));
		} else {
			File vanilla = getFile(id);
			if (vanilla.exists()) {
				in = new FastBufferedInputStream(new GZIPInputStream(new FileInputStream(vanilla)));
			} else {
				return null;
			}
		}
		
		try (in) {
			DataInputStream dis = new DataInputStream(in);
			NbtCompound nbt = NbtIo.read(dis);
			int version = NbtHelper.getDataVersion(nbt, 1343);
			nbt = DataFixTypes.SAVED_DATA.update(dataFixer, nbt, version, dataVersion);
			return nbt;
		}
	}
	
}
