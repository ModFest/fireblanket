package net.modfest.fireblanket.mixinsupport;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.FileNameDateFormatter;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueOutput;
import net.modfest.fireblanket.util.IOUnaryOperation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class ZestyPlayerSaveHandler extends PlayerDataStorage {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean AVOID_ZTSD = Boolean.getBoolean("fireblanket.saveAsDat");

	private final Path playerDataDir;

	public ZestyPlayerSaveHandler(LevelStorageAccess session, DataFixer dataFixer) {
		super(session, dataFixer);
		this.playerDataDir = session.getLevelPath(LevelResource.PLAYER_DATA_DIR);
	}

	private void backupPlayerData(
		final NameAndId player,
		final Path failed,
		final String extension
	) {
		if (!Files.isRegularFile(failed)) {
			return;
		}

		Path backup = this.playerDataDir.resolve(player.id() + "_corrupted_" + LocalDateTime.now().format(
			FileNameDateFormatter.FORMATTER) + "." + extension);

		try {
			Files.copy(failed, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (Exception e) {
			LOGGER.warn("Failed to copy the player.dat file for {}", player, e);
		}
	}

	private @Nullable CompoundTag loadPlayerData(
		final NameAndId player,
		final String extension,
		final IOUnaryOperation<InputStream> decoder
	) {
		Path path = this.playerDataDir.resolve(player.id() + "." + extension);
		if (Files.isRegularFile(path)) {
			try (InputStream in = new FastBufferedInputStream(decoder.apply(Files.newInputStream(path)))) {
				return NbtIo.read(new DataInputStream(in));
			} catch (Exception e) {
				LOGGER.warn("Failed to load player data for {} at {}", player, path, e);
				backupPlayerData(player, path, extension);
			}
		}

		return null;
	}

	private @Nullable CompoundTag tryLoadPlayerData(NameAndId player) {
		CompoundTag nbt = loadPlayerData(player, "zat", ZstdInputStream::new);
		if (nbt != null) {
			return nbt;
		}

		nbt = loadPlayerData(player, "zat_old", ZstdInputStream::new);
		if (nbt != null) {
			return nbt;
		}

		nbt = loadPlayerData(player, "dat", GZIPInputStream::new);
		if (nbt != null) {
			return nbt;
		}

		return loadPlayerData(player, "dat_old", GZIPInputStream::new);
	}

	@Override
	public Optional<CompoundTag> load(NameAndId player) {
		CompoundTag nbt = tryLoadPlayerData(player);

		if (nbt == null) {
			return Optional.empty();
		}

		try {
			int ver = NbtUtils.getDataVersion(nbt, -1);
			nbt = DataFixTypes.PLAYER.updateToCurrentVersion(fixerUpper, nbt, ver);
			return Optional.of(nbt);
		} catch (Exception e) {
			LOGGER.warn("Failed to load player data for {}", player);
			return Optional.empty();
		}
	}

	@Override
	public void save(Player player) {
		if (AVOID_ZTSD) {
			super.save(player);
			return;
		}
		try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(player.problemPath(), LOGGER)) {
			TagValueOutput nbtWriteView = TagValueOutput.createWithContext(logging, player.registryAccess());
			player.saveWithoutId(nbtWriteView);
			Path tmp = Files.createTempFile(this.playerDataDir, player.getStringUUID() + "-", ".zat");
			try (ZstdOutputStream z = new ZstdOutputStream(Files.newOutputStream(tmp))) {
				z.setChecksum(true);
				z.setLevel(6);
				NbtIo.writeUnnamedTagWithFallback(nbtWriteView.buildResult(), new DataOutputStream(z));
			}
			Path tgt = this.playerDataDir.resolve(player.getStringUUID() + ".zat");
			Path backup = this.playerDataDir.resolve(player.getStringUUID() + ".zat_old");
			Util.safeReplaceFile(tgt, tmp, backup);

			Path oldTgt = this.playerDataDir.resolve(player.getStringUUID() + ".dat");
			Path oldBackup = this.playerDataDir.resolve(player.getStringUUID() + ".dat_old");
			Files.deleteIfExists(oldTgt);
			Files.deleteIfExists(oldBackup);
		} catch (Exception e) {
			LOGGER.warn("Failed to save player data for {}", player.getName().getString());
		}
	}
}
