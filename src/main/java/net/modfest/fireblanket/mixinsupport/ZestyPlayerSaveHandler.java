package net.modfest.fireblanket.mixinsupport;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.level.storage.LevelStorage.Session;
import org.slf4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class ZestyPlayerSaveHandler extends PlayerSaveHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean AVOID_ZTSD = Boolean.getBoolean("fireblanket.saveAsDat");

	private final File playerDataDir;

	public ZestyPlayerSaveHandler(Session session, DataFixer dataFixer) {
		super(session, dataFixer);
		this.playerDataDir = session.getDirectory(WorldSavePath.PLAYERDATA).toFile();
	}

//	@Override
//	public Optional<NbtCompound> loadPlayerData(PlayerEntity player) {
//		try {
//			InputStream in;
//			File zstd = new File(playerDataDir, player.getUuidAsString() + ".zat");
//			if (zstd.isFile()) {
//				in = new FastBufferedInputStream(new ZstdInputStream(new FileInputStream(zstd)));
//			} else {
//				File vanilla = new File(playerDataDir, player.getUuidAsString() + ".dat");
//				if (vanilla.isFile()) {
//					in = new FastBufferedInputStream(new GZIPInputStream(new FileInputStream(zstd)));
//				} else {
//					return Optional.empty();
//				}
//			}
//			try (in) {
//				NbtCompound nbt = NbtIo.readCompound(new DataInputStream(in));
//				int ver = NbtHelper.getDataVersion(nbt, -1);
//				player.readNbt(DataFixTypes.PLAYER.update(dataFixer, nbt, ver));
//				return Optional.of(nbt);
//			}
//		} catch (Exception e) {
//			LOGGER.warn("Failed to load player data for {}", player.getName().getString());
//			return Optional.empty();
//		}
//	}

	@Override
	public void savePlayerData(PlayerEntity player) {
//		if (AVOID_ZTSD) {
			super.savePlayerData(player);
//			return;
//		}
//		try {
//			NbtCompound nbt = player.writeNbt(new NbtCompound());
//			File tmp = File.createTempFile(player.getUuidAsString() + "-", ".zat", playerDataDir);
//			try (ZstdOutputStream z = new ZstdOutputStream(new FileOutputStream(tmp))) {
//				z.setChecksum(true);
//				z.setLevel(6);
//				NbtIo.write(nbt, new DataOutputStream(z));
//			}
//			File tgt = new File(playerDataDir, player.getUuidAsString() + ".zat");
//			File backup = new File(playerDataDir, player.getUuidAsString() + ".zat_old");
//			Util.backupAndReplace(tgt.toPath(), tmp.toPath(), backup.toPath());
//			File oldTgt = new File(playerDataDir, player.getUuidAsString() + ".dat");
//			File oldBackup = new File(playerDataDir, player.getUuidAsString() + ".dat_old");
//			oldTgt.delete();
//			if (backup.exists()) oldBackup.delete();
//		} catch (Exception e) {
//			LOGGER.warn("Failed to save player data for {}", player.getName().getString());
//		}
	}
}
