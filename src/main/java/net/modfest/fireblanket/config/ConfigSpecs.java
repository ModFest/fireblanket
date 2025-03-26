package net.modfest.fireblanket.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigSpecs {
	public static final ConfigSpec<List<String>> PRIVILEGED_USERS = new ConfigSpec<>("privileged-users", "Privileged users",
		"""
			Specifies users that are allowed to perform potentially extremely destructive commands. Treat with EXTREME CAUTION.
			Reccomended to match users given the "netadmin" role.
			""", "jaskarth,unascribed", ConfigParsers.STRING_LIST);

	public static final ConfigSpec<List<String>> MIXIN_SCRAM = new ConfigSpec<>("mixin-scram", "Mixin SCRAM",
		"""
			Force disables mixins from being loaded. Treat with EXTREME CAUTION, as certain mixins are required for functionality.
			The format is a regex, so it is possible to disable an entire package.
			Example:
			mixin-scram: ai.MixinTemptGoal,client.hooks.*;
			""", "", ConfigParsers.STRING_LIST);

	public static final ConfigSpec<Integer> FORCED_LOAD_RADIUS = new ConfigSpec<>("forced-load-radius", "Forced load radius",
		"""
			Chunk accesses and loading can become very expensive for a large fest. This option keeps a predefined region of chunks
			loaded (but not ticking), so that cold accesses don't suffer a fetch from disk. WARNING: This option can dramatically
			increase the memory usage of the server, so treat with care! A value of '0' disables this optimization.
			Value is defined in blocks.
			""", "0", ConfigParsers.INTEGER);

	public static final ConfigSpec<Boolean> FLATTEN_CHUNK_PALETTES = new ConfigSpec<>("flatten-chunk-palettes", "Flatten chunk palettes",
		"""
			Flatten chunk data to use an array instead of looking up a palette each time. This allows significantly faster lookups
			at the cost of tremendous memory usage.
			""", "no", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<Boolean> ALLOW_FOOTGUNS = new ConfigSpec<>("allow-footguns", "Allow Footguns",
		"""
			Disable pre-emptive disabling of @e selectors unless 'force=true' is also specified.
			""", "no", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<Boolean> AVOID_ZSTD = new ConfigSpec<>("avoid-zstd", "Avoid zstd",
		"""
			Disables the usage of zstd to save world data and compress network connections.
			""", "no", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<Boolean> GAMEPLAY_CHANGES = new ConfigSpec<>("gameplay-changes", "Gameplay Changes",
		"""
			Allow altering features in a way that impacts gameplay. This disables some entity AI and how certain sounds are played.
			""", "no", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<Integer> ASYNC_PACKET_THREADS = new ConfigSpec<>("async-packet-threads", "Async Packet Threads",
		"""
			Configures how many threads are used to send packets on the dedicated server.
			""", "4", ConfigParsers.INTEGER);


	public static final List<ConfigSpec<?>> ALL_SPECS = new ArrayList<>();

	public static final Map<String, ConfigSpec<?>> BY_NAME = new HashMap<>();

	private static void buildMap() {
		for (ConfigSpec<?> spec : ALL_SPECS) {
			BY_NAME.put(spec.name(), spec);
		}
	}

	static {
		ALL_SPECS.add(PRIVILEGED_USERS);
		ALL_SPECS.add(MIXIN_SCRAM);
		ALL_SPECS.add(FORCED_LOAD_RADIUS);
		ALL_SPECS.add(FLATTEN_CHUNK_PALETTES);
		ALL_SPECS.add(ALLOW_FOOTGUNS);
		ALL_SPECS.add(AVOID_ZSTD);
		ALL_SPECS.add(GAMEPLAY_CHANGES);
		ALL_SPECS.add(ASYNC_PACKET_THREADS);

		// We have registries at home:
		buildMap();
	}
}
