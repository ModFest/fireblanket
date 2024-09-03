package net.modfest.fireblanket;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class FireblanketConstants {
	public static final String MOD_ID = "fireblanket";

	public static final TagKey<Block> BLOCK_INTERACTION_RESTRICTED = tag(RegistryKeys.BLOCK, "block_interaction_restricted");
	public static final TagKey<Item> ITEM_INTERACTION_RESTRICTED = tag(RegistryKeys.ITEM, "item_interaction_restricted");
	public static final TagKey<EntityType<?>> ENTITY_INTERACTION_RESTRICTED = tag(RegistryKeys.ENTITY_TYPE, "entity_interaction_restricted");

	public static final TagKey<EntityType<?>> ENTITY_ATTACK_RESTRICTED = tag(RegistryKeys.ENTITY_TYPE, "entity_attack_restricted");

	public static Identifier id(String id) { return Identifier.of(MOD_ID, id); }

	private static <T> TagKey<T> tag(RegistryKey<? extends Registry<T>> reg, String id) {
		return TagKey.of(reg, id(id));
	}
}
