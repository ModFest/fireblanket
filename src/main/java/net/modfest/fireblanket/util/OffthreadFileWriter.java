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
			//noinspection resource
			final AsyncWriter writer = FILE_MAP.computeIfAbsent(path, $ -> {
				try {
					return AsyncWriter.bufferedWriter(path);
				} catch (IOException e) {
					throw new RuntimeException("Failed to open " + path, e);
				}
			});

			writer.write(content);
		} catch (IOException io) {
			// TODO: maybe swallow this depending on context
			throw new RuntimeException("Failed to write to " + path, io);
		}
	}
}
