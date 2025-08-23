package net.modfest.fireblanket.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OffthreadFileWriter {
	private static final Map<Path, AsyncWriter> FILE_MAP = new ConcurrentHashMap<>();

	@Deprecated
	public static void write(String content, File file) {
		write(file.toPath(), content);
	}

	public static void write(Path path, String content) {
		try {
			// TODO: figure out a better locking strategy
			AsyncWriter writer;
			synchronized (FILE_MAP) {
				writer = FILE_MAP.get(path);
				if (writer == null) {
					writer = AsyncWriter.bufferedWriter(path);
					FILE_MAP.put(path, writer);
				}
			}

			writer.write(content);
		} catch (IOException io) {
			// TODO: maybe swallow this depending on context
			throw new RuntimeException("cannot write to " + path, io);
		}
	}
}
