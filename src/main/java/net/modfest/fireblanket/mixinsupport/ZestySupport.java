package net.modfest.fireblanket.mixinsupport;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Ampflower
 */
public final class ZestySupport {
	public static @Nullable Path zestifyIfVanilla(Path path) {
		final Path parent = path.getParent();
		final String fileName = path.getFileName().toString();

		if (!fileName.endsWith(".dat")) {
			// Don't know what you want, but alright.
			// No zstd for you.
			return null;
		}

		return parent.resolve(fileName.substring(0, fileName.length() - 4) + ".zat");
	}

	public static InputStream inputStream(Path path) throws IOException {
		return new FastBufferedInputStream(new ZstdInputStream(Files.newInputStream(path)));
	}

	public static OutputStream outputStream(Path path) throws IOException {
		return new ZstdOutputStream(Files.newOutputStream(path));
	}
}
