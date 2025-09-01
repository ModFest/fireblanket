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

	public static final ConfigSpec<List<String>> BANNED_ITEMS = new ConfigSpec<>("banned-items", "Banned Items",
		"""
			Removes all instances of the listed items from a player's inventory on tick. Useful for emergency moderation.
			Example:
			banned-items: minecraft:iron_sword,minecraft:diamond_sword
			""", "", ConfigParsers.STRING_LIST);

	public static final ConfigSpec<Boolean> LOG_COMMAND_ERRORS = new ConfigSpec<>("log-command-errors", "Log command errors",
		"""
			Log unexpected command exceptions to the server logs.
			""", "no", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<Boolean> LOG_PLAYER_COMMANDS = new ConfigSpec<>("log-player-commands", "Log player commands",
		"""
			Logs all player execute commands into a text file.
			""", "yes", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<List<String>> IGNORED_COMMAND_LOGS = new ConfigSpec<>("ignored-command-logs", "Ignored command logs",
		"""
			A list of base level commands to ignore when logging player commands.
			""", "msg,me,say,teammsg", ConfigParsers.STRING_LIST);

	public static final ConfigSpec<Boolean> TATTLETALE_COMMANDS = new ConfigSpec<>(
		"tattletale-commands",
		"Reveals the true command runner",
		"""
			Reveals the true command runner when `/execute as`, area-tools and more are involved.
						
			Toggling this on...
			- Tags all `/execute as @s run` executions with the true command runner if differing and found.
			- Tags all command blocks with its current information, including its type, location, creator and updater.
			- May increase bandwidth, each message has to contain more metadata
			""", "yes", ConfigParsers.BOOLEAN);

	public static final ConfigSpec<List<String>> LOCKED_GAMERULES = new ConfigSpec<>("locked-gamerules", "Ignored command logs",
		"""
			A list of gamerules that are not allowed to be changed by non team/organizers.
			""", "sendCommandFeedback,reducedDebugInfo,logAdminCommands", ConfigParsers.STRING_LIST);

	public static final ConfigSpec<String> TRACE_LEVEL = new ConfigSpec<>("trace-level", "Stack Tracer Level",
		"""
			The level of stack tracing that is done to ensure consistency with push/pops.
						
			Note: This has a generally high impact on performance, and is left off by default.
						
			Valid modes:
						
			- none - Disables the tracer
			- mod - Traces at a mod level (will miss root causes)
			- package - Traces at a package level (may miss root causes)
			- class - Traces at a class level (may miss in-class trampolines & mixins)
			- function - Traces at a function level (the strictest available)
			""", "none", ConfigParsers.STRING
	);


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
		ALL_SPECS.add(BANNED_ITEMS);
		ALL_SPECS.add(LOG_COMMAND_ERRORS);
		ALL_SPECS.add(LOG_PLAYER_COMMANDS);
		ALL_SPECS.add(IGNORED_COMMAND_LOGS);
		ALL_SPECS.add(TATTLETALE_COMMANDS);
		ALL_SPECS.add(LOCKED_GAMERULES);
		ALL_SPECS.add(TRACE_LEVEL);

		// We have registries at home:
		buildMap();
	}
}
