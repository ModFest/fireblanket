package net.modfest.fireblanket.mixinsupport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.storage.LevelStorage.Session;

public class ZestyWorldSaveHandler extends WorldSaveHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final File playerDataDir;
	
	public ZestyWorldSaveHandler(Session session, DataFixer dataFixer) {
		super(session, dataFixer);
		this.playerDataDir = session.getDirectory(WorldSavePath.PLAYERDATA).toFile();
	}
	
	@Override
	public NbtCompound loadPlayerData(PlayerEntity player) {
		try {
			InputStream in;
			File zstd = new File(playerDataDir, player.getUuidAsString()+".zat");
			if (zstd.isFile()) {
				in = new FastBufferedInputStream(new ZstdInputStream(new FileInputStream(zstd)));
			} else {
				File vanilla = new File(playerDataDir, player.getUuidAsString()+".dat");
				if (vanilla.isFile()) {
					in = new FastBufferedInputStream(new GZIPInputStream(new FileInputStream(zstd)));
				} else {
					return null;
				}
			}
			try (in) {
				NbtCompound nbt = NbtIo.read(new DataInputStream(in));
				int ver = NbtHelper.getDataVersion(nbt, -1);
				player.readNbt(DataFixTypes.PLAYER.update(dataFixer, nbt, ver));
				return nbt;
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to load player data for {}", player.getName().getString());
			return null;
		}
	}
	
	@Override
	public void savePlayerData(PlayerEntity player) {
		try {
			NbtCompound nbt = player.writeNbt(new NbtCompound());
			File tmp = File.createTempFile(player.getUuidAsString()+"-", ".zat", playerDataDir);
			try (ZstdOutputStream z = new ZstdOutputStream(new FileOutputStream(tmp))) {
				z.setChecksum(true);
				z.setLevel(6);
				NbtIo.write(nbt, new DataOutputStream(z));
			}
			File tgt = new File(playerDataDir, player.getUuidAsString()+".zat");
			File backup = new File(playerDataDir, player.getUuidAsString()+".zat_old");
			Util.backupAndReplace(tgt, tmp, backup);
			File oldTgt = new File(playerDataDir, player.getUuidAsString()+".dat");
			File oldBackup = new File(playerDataDir, player.getUuidAsString()+".dat_old");
			oldTgt.delete();
			if (backup.exists()) oldBackup.delete();
		} catch (Exception e) {
			LOGGER.warn("Failed to save player data for {}", player.getName().getString());
		}
	}
	
	@Override
	public String[] getSavedPlayerIds() {
		if (playerDataDir.isDirectory()) {
			Set<String> set = new ObjectOpenHashSet<>();
			for (String s : playerDataDir.list()) {
				if (s.endsWith(".dat") || s.endsWith(".zat")) {
					set.add(s.substring(0, s.length()-4));
				}
			}
			return set.toArray(String[]::new);
		}
		return new String[0];
	}

}
