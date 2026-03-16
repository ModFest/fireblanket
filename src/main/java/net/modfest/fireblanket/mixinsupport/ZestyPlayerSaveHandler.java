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
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.DateTimeFormatters;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.level.storage.LevelStorage.Session;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class ZestyPlayerSaveHandler extends PlayerSaveHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean AVOID_ZTSD = Boolean.getBoolean("fireblanket.saveAsDat");

	// I don't know why this isn't just a constant there.
	private static final DateTimeFormatter FORMATTER = DateTimeFormatters.create();

	private final Path playerDataDir;

	public ZestyPlayerSaveHandler(Session session, DataFixer dataFixer) {
		super(session, dataFixer);
		this.playerDataDir = session.getDirectory(WorldSavePath.PLAYERDATA);
	}

	private void backupPlayerData(
		final PlayerEntity player,
		final Path failed,
		final String extension
	) {
		if (!Files.isRegularFile(failed)) {
			return;
		}

		Path backup = this.playerDataDir.resolve(player.getUuidAsString() + "_corrupted_" + LocalDateTime.now().format(
			FORMATTER) + "." + extension);

		try {
			Files.copy(failed, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (Exception e) {
			LOGGER.warn("Failed to copy the player.dat file for {}", player.getName().getString(), e);
		}
	}

	private @Nullable NbtCompound loadPlayerData(
		final PlayerEntity player,
		final String extension,
		final IOUnaryOperation<InputStream> decoder
	) {
		Path path = this.playerDataDir.resolve(player.getUuidAsString() + "." + extension);
		if (Files.isRegularFile(path)) {
			try (InputStream in = new FastBufferedInputStream(decoder.apply(Files.newInputStream(path)))) {
				return NbtIo.readCompound(new DataInputStream(in));
			} catch (Exception e) {
				LOGGER.warn("Failed to load player data for {} at {}", player.getName().getString(), path, e);
				backupPlayerData(player, path, extension);
			}
		}

		return null;
	}

	private @Nullable NbtCompound tryLoadPlayerData(PlayerEntity player) {
		NbtCompound nbt = loadPlayerData(player, "zat", ZstdInputStream::new);
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
	public Optional<ReadView> loadPlayerData(PlayerEntity player, ErrorReporter errorReporter) {
		NbtCompound nbt = tryLoadPlayerData(player);

		if (nbt == null) {
			return Optional.empty();
		}

		try {
			int ver = NbtHelper.getDataVersion(nbt, -1);
			nbt = DataFixTypes.PLAYER.update(dataFixer, nbt, ver);
			ReadView readView = NbtReadView.create(errorReporter, player.getRegistryManager(), nbt);
			player.readData(readView);
			return Optional.of(readView);
		} catch (Exception e) {
			LOGGER.warn("Failed to load player data for {}", player.getName().getString());
			return Optional.empty();
		}
	}

	@Override
	public void savePlayerData(PlayerEntity player) {
		if (AVOID_ZTSD) {
			super.savePlayerData(player);
			return;
		}
		try (ErrorReporter.Logging logging = new ErrorReporter.Logging(player.getErrorReporterContext(), LOGGER)) {
			NbtWriteView nbtWriteView = NbtWriteView.create(logging, player.getRegistryManager());
			player.writeData(nbtWriteView);
			Path tmp = Files.createTempFile(this.playerDataDir, player.getUuidAsString() + "-", ".zat");
			try (ZstdOutputStream z = new ZstdOutputStream(Files.newOutputStream(tmp))) {
				z.setChecksum(true);
				z.setLevel(6);
				NbtIo.write(nbtWriteView.getNbt(), new DataOutputStream(z));
			}
			Path tgt = this.playerDataDir.resolve(player.getUuidAsString() + ".zat");
			Path backup = this.playerDataDir.resolve(player.getUuidAsString() + ".zat_old");
			Util.backupAndReplace(tgt, tmp, backup);

			Path oldTgt = this.playerDataDir.resolve(player.getUuidAsString() + ".dat");
			Path oldBackup = this.playerDataDir.resolve(player.getUuidAsString() + ".dat_old");
			Files.deleteIfExists(oldTgt);
			Files.deleteIfExists(oldBackup);
		} catch (Exception e) {
			LOGGER.warn("Failed to save player data for {}", player.getName().getString());
		}
	}
}
