package net.modfest.fireblanket.util;

import net.modfest.fireblanket.Fireblanket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OffthreadFileWriter extends Thread {
	private static final Thread THREAD = new OffthreadFileWriter();
	private static final Map<File, String> FILE_MAP = new HashMap<>();
	private static final Object WAIT_LOCK = new Object();

	public OffthreadFileWriter() {
	    super("Off-thread File Writer");
		this.setDaemon(true);
	}

	@Override
	public void run() {
		try {
			while (!this.isInterrupted()) {
				synchronized (WAIT_LOCK) {
					WAIT_LOCK.wait();
				}

				// Copy the set as it is a reference to a field in the map class, and changes to the map are reflected into it
				Set<Map.Entry<File, String>> entries = Set.copyOf(FILE_MAP.entrySet());
				FILE_MAP.clear();

				for (Map.Entry<File, String> entry : entries) {
					try (FileWriter writer = new FileWriter(entry.getKey(), true)) {
						writer.append(entry.getValue());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} catch (InterruptedException ignored) {}
		Fireblanket.LOGGER.info("Fireblanket async file handler closed");
	}

	public static void write(String content, File file) {
		FILE_MAP.merge(file, content, (s, s2) -> s + s2);
		synchronized (WAIT_LOCK) {
			WAIT_LOCK.notifyAll();
		}
	}

	static {
		THREAD.start();
	}
}
