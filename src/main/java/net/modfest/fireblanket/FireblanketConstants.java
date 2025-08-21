package net.modfest.fireblanket;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

public class FireblanketConstants {
	public static final String MOD_ID = "fireblanket";

	public static final TagKey<Block> BLOCK_INTERACTION_RESTRICTED = tag(RegistryKeys.BLOCK, "block_interaction_restricted");
	public static final TagKey<Block> BLOCK_SNEAK_INTERACTION_RESTRICTED = tag(RegistryKeys.BLOCK, "block_sneak_interaction_restricted");
	public static final TagKey<Item> ITEM_INTERACTION_RESTRICTED = tag(RegistryKeys.ITEM, "item_interaction_restricted");
	public static final TagKey<Item> ITEM_SNEAK_INTERACTION_RESTRICTED = tag(RegistryKeys.ITEM, "item_sneak_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_INTERACTION_RESTRICTED = tag(RegistryKeys.ENTITY_TYPE, "entity_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_SNEAK_INTERACTION_RESTRICTED = tag(RegistryKeys.ENTITY_TYPE, "entity_sneak_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_SUMMON_DISALLOWED = tag(RegistryKeys.ENTITY_TYPE, "entity_summon_disallowed");

	public static final TagKey<EntityType<?>> ENTITY_ATTACK_RESTRICTED = tag(RegistryKeys.ENTITY_TYPE, "entity_attack_restricted");

	public static final Path FIREBLANKET_DIR = FabricLoader.getInstance().getGameDir().resolve("fireblanket");;
	public static final File COMMAND_LOGS_FILE = FIREBLANKET_DIR.resolve("player_commands.log").toFile();

	public static final DateTimeFormatter SIMPLE_TIME_FORMATTER =  new DateTimeFormatterBuilder()
		.appendValue(HOUR_OF_DAY, 2)
		.appendLiteral(':')
		.appendValue(MINUTE_OF_HOUR, 2)
		.appendLiteral(':')
		.appendValue(SECOND_OF_MINUTE, 2)
		.toFormatter();

	public static Identifier id(String id) { return Identifier.of(MOD_ID, id); }

	private static <T> TagKey<T> tag(RegistryKey<? extends Registry<T>> reg, String id) {
		return TagKey.of(reg, id(id));
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
