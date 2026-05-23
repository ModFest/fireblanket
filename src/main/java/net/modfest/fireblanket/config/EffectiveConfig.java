package net.modfest.fireblanket.config;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Runtime effective configuration.
 *
 * @author Ampflower
 */
// TODO: Eventually, we'll want runtime adjusted entries to be saved into the config
//  on an explicit /fireblanket config save.
@NullMarked
public final class EffectiveConfig {

	public static final Set<Item> BANNED_ITEMS = Collections.newSetFromMap(new IdentityHashMap<>());
	public static final Set<Identifier> BANNED_ITEMS_RAW = new HashSet<>();

	public static final Set<EntityType<?>> ADVENTURE_FIXED_ENTITIES = Collections.newSetFromMap(new IdentityHashMap<>());
	public static final Set<Identifier> ADVENTURE_FIXED_ENTITIES_RAW = new HashSet<>();

	/**
	 * One-time init of the configuration, caching a set of entries.
	 */
	public static void init() {
		BANNED_ITEMS_RAW.clear();
		ADVENTURE_FIXED_ENTITIES_RAW.clear();

		iterateIds(ConfigSpecs.BANNED_ITEMS, BANNED_ITEMS_RAW::add);
		iterateIds(ConfigSpecs.ADVENTURE_FIXED_ENTITIES, ADVENTURE_FIXED_ENTITIES_RAW::add);
	}

	/**
	 * Explicitly reinitializes parts of the effective config that caches registry or level data.
	 */
	public static void apply(RegistryAccess access) {
		BANNED_ITEMS.clear();
		ADVENTURE_FIXED_ENTITIES.clear();

		iterateIds(access, Registries.ITEM, BANNED_ITEMS_RAW, BANNED_ITEMS::add);
		iterateIds(access, Registries.ENTITY_TYPE, ADVENTURE_FIXED_ENTITIES_RAW, ADVENTURE_FIXED_ENTITIES::add);
	}

	// region Item Ban
	public static boolean isItemBanned(final RegistryAccess access, final ItemStack container) {
		return isItemBanned(access, container.getItem());
	}

	public static boolean isItemBanned(final RegistryAccess access, final Item value) {
		// Alt lookup: access.lookup(Registries.ITEM).get().getKey(item)
		return BANNED_ITEMS.contains(value);
	}

	public static boolean addItemBan(final RegistryAccess access, final String raw) {
		return add(access, Registries.ITEM, ConfigSpecs.BANNED_ITEMS, BANNED_ITEMS_RAW, BANNED_ITEMS, raw);
	}

	public static boolean removeItemBan(final RegistryAccess access, final String raw) {
		return remove(access, Registries.ITEM, ConfigSpecs.BANNED_ITEMS, BANNED_ITEMS_RAW, BANNED_ITEMS, raw);
	}
	// endregion

	// region Adventure Fixed Entities
	public static boolean isAdventureFixedEntity(final Entity container) {
		return isAdventureFixedEntity(container.registryAccess(), container.getType());
	}

	public static boolean isAdventureFixedEntity(final RegistryAccess access, final EntityType<?> value) {
		return ADVENTURE_FIXED_ENTITIES.contains(value);
	}

	public static boolean addAdventureFixedEntity(final RegistryAccess access, final String raw) {
		return add(
			access,
			Registries.ENTITY_TYPE,
			ConfigSpecs.ADVENTURE_FIXED_ENTITIES,
			ADVENTURE_FIXED_ENTITIES_RAW,
			ADVENTURE_FIXED_ENTITIES,
			raw
		);
	}

	public static boolean removeAdventureFixedEntity(final RegistryAccess access, final String raw) {
		return remove(
			access,
			Registries.ENTITY_TYPE,
			ConfigSpecs.ADVENTURE_FIXED_ENTITIES,
			ADVENTURE_FIXED_ENTITIES_RAW,
			ADVENTURE_FIXED_ENTITIES,
			raw
		);
	}
	// endregion

	// region Config
	private static <T> boolean add(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final ConfigSpec<? extends Collection<String>> configSpec,
		final Collection<Identifier> ids,
		final Collection<? super T> values,
		final String raw
	) {
		if (raw.isEmpty()) {
			return false;
		}

		final Identifier id = Identifier.tryParse(raw);
		if (id == null || !ids.add(id)) {
			return false;
		}

		final @Nullable T value = access(access, registry, id);
		if (value != null) {
			values.add(value);
		}

		return true;
	}

	private static <T> boolean remove(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final ConfigSpec<? extends Collection<String>> configSpec,
		final Collection<Identifier> ids,
		final Collection<? super T> values,
		final String raw
	) {
		if (raw.isEmpty()) {
			return false;
		}

		final Identifier id = Identifier.tryParse(raw);
		if (id == null || !ids.remove(id)) {
			return false;
		}

		final @Nullable T value = access(access, registry, id);
		if (value != null) {
			values.remove(value);
		}

		return true;
	}
	// endregion

	private static <T> @Nullable T access(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final Identifier id
	) {
		return access.get(ResourceKey.create(registry, id)).map(Holder::value).orElse(null);
	}

	private static <T> void iterate(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final ConfigSpec<? extends Iterable<String>> configSpec,
		final Consumer<T> consumer
	) {
		iterate(access, registry, FireblanketConfig.get(configSpec), consumer);
	}

	private static <T> void iterate(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final Iterable<String> iterable,
		final Consumer<T> consumer
	) {
		for (final String raw : iterable) {
			final Identifier id = Identifier.tryParse(raw);
			if (id == null) {
				continue;
			}

			final Optional<? extends Holder<T>> optional = access.get(ResourceKey.create(registry, id));
			if (optional.isEmpty()) {
				continue;
			}

			consumer.accept(optional.get().value());
		}
	}

	private static void iterateIds(
		final ConfigSpec<? extends Iterable<String>> configSpec,
		final Consumer<Identifier> consumer
	) {
		for (final String value : FireblanketConfig.get(configSpec)) {
			if (value.isEmpty()) {
				continue;
			}

			final Identifier id = Identifier.tryParse(value);
			if (id == null) {
				continue;
			}

			consumer.accept(id);
		}
	}

	private static <T> void iterateIds(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final Iterable<Identifier> iterable,
		final Consumer<T> consumer
	) {
		for (final Identifier id : iterable) {
			final Optional<? extends Holder<T>> optional = access.get(ResourceKey.create(registry, id));
			if (optional.isEmpty()) {
				continue;
			}

			consumer.accept(optional.get().value());
		}
	}
}
