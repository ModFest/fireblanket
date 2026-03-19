package net.modfest.fireblanket;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.YEAR;

public final class FireblanketConstants {
	public static final String MOD_ID = "fireblanket";

	public static final TagKey<Block> BLOCK_INTERACTION_RESTRICTED = tag(Registries.BLOCK, "block_interaction_restricted");
	public static final TagKey<Block> BLOCK_SNEAK_INTERACTION_RESTRICTED = tag(Registries.BLOCK, "block_sneak_interaction_restricted");
	public static final TagKey<Item> ITEM_INTERACTION_RESTRICTED = tag(Registries.ITEM, "item_interaction_restricted");
	public static final TagKey<Item> ITEM_SNEAK_INTERACTION_RESTRICTED = tag(Registries.ITEM, "item_sneak_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_INTERACTION_RESTRICTED = tag(Registries.ENTITY_TYPE, "entity_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_SNEAK_INTERACTION_RESTRICTED = tag(Registries.ENTITY_TYPE, "entity_sneak_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_SUMMON_DISALLOWED = tag(Registries.ENTITY_TYPE, "entity_summon_disallowed");

	public static final TagKey<EntityType<?>> ENTITY_ATTACK_RESTRICTED = tag(Registries.ENTITY_TYPE, "entity_attack_restricted");

	public static final Path FIREBLANKET_DIR = FabricLoader.getInstance().getGameDir().resolve("fireblanket");
	public static final File COMMAND_LOGS_FILE = FIREBLANKET_DIR.resolve("player_commands.log").toFile();

	public static final DateTimeFormatter SIMPLE_TIME_FORMATTER = new DateTimeFormatterBuilder()
		.appendValue(YEAR, 4)
		.appendLiteral('-')
		.appendValue(MONTH_OF_YEAR, 2)
		.appendLiteral('-')
		.appendValue(DAY_OF_MONTH, 2)
		.appendLiteral(' ')
		.appendValue(HOUR_OF_DAY, 2)
		.appendLiteral(':')
		.appendValue(MINUTE_OF_HOUR, 2)
		.appendLiteral(':')
		.appendValue(SECOND_OF_MINUTE, 2)
		.toFormatter();

	/**
	 * @see net.modfest.fireblanket.mixin.opto.MixinCommandBlockExecutor
	 */
	public static final DateTimeFormatter COMMAND_EXECUTOR_FORMATTER = new DateTimeFormatterBuilder()
		.appendLiteral('[')
		.appendValue(HOUR_OF_DAY, 2)
		.appendLiteral(':')
		.appendValue(MINUTE_OF_HOUR, 2)
		.appendLiteral(':')
		.appendValue(SECOND_OF_MINUTE, 2)
		.appendLiteral("] ")
		.toFormatter();

	public static Identifier id(String id) {
		return Identifier.fromNamespaceAndPath(MOD_ID, id);
	}

	private static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> reg, String id) {
		return TagKey.create(reg, id(id));
	}

	static {
		if (!Files.exists(FIREBLANKET_DIR)) {
			try {
				Files.createDirectories(FIREBLANKET_DIR);
			} catch (IOException e) {
				throw new RuntimeException("Creating data directory", e);
			}
		}
	}
}
