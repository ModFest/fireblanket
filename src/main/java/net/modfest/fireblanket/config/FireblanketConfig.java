package net.modfest.fireblanket.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FireblanketConfig {
	private static Map<ConfigSpec<?>, Object> map = new HashMap<>();

	public static <T> T get(ConfigSpec<T> spec) {
		return (T) map.computeIfAbsent(spec, k -> spec.parser().parse(spec.defaultValue()));
	}

	private static <T> void put(ConfigSpec<T> spec, Object value) {
		map.put(spec, value);
	}

	public static void init() {
		Path configs = FabricLoader.getInstance().getConfigDir().resolve("fireblanket");
		Path file = configs.resolve("fireblanket.txt");

		try {
			if (Files.exists(file)) {
				parse(Files.readAllLines(file));
			} else {
				writeDefault(file);
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to parse fireblanket.txt", e);
		}
	}

	private static void writeDefault(Path path) {
		List<String> strings = new ArrayList<>();
		strings.add("### Fireblanket main config");
		strings.add("# Octothorpes are comments and can only be found at the start of a line.");

		for (ConfigSpec<?> spec : ConfigSpecs.ALL_SPECS) {
			strings.add("");

			strings.add("## " + spec.prettyName());

			for (String str : spec.description().split("\n")) {
				strings.add("# " + str);
			}

			String v = spec.name() + ": " + spec.defaultValue() + ";";
			strings.add(v);
		}

		try {
			Files.write(path, strings);
		} catch (IOException e) {
			throw new RuntimeException("Unable to write fireblanket.txt", e);
		}
	}

	public static void parse(List<String> text) {
		Set<ConfigSpec<?>> specs = new HashSet<>(ConfigSpecs.ALL_SPECS);

		// TODO: proper multiline parsing
		// ^([\w|-]+):\s+(.*);
		Pattern pattern = Pattern.compile("^([\\w|-]+):\\s+(.*);");
		for (String string : text) {
			string = string.strip();

			if (string.startsWith("#")) {
				continue;
			}

			Matcher matcher = pattern.matcher(string);
			if (matcher.matches()) {
				String name = matcher.group(1);
				String value = matcher.group(2);
				ConfigSpec<?> spec = ConfigSpecs.BY_NAME.get(name);
				if (spec == null) {
					throw new RuntimeException("Unknown config option " + name);
				}

				if (!specs.remove(spec)) {
					throw new RuntimeException("Duplicate config option " + name);
				}

				put(spec, spec.parser().parse(value));
			}
		}

		System.out.println("Didn't find values for these config values, using default: " + specs);
	}
}
