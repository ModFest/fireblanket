package net.modfest.fireblanket.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Ampflower
 **/
public final class RegistryUtil {

	private static final Pattern SPACES = Pattern.compile("\\s+");

	public static <T> Iterable<? extends Holder<T>> getResources(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final String raw
	) {
		if (raw.isBlank()) {
			return Set.of();
		}

		final String[] raws = SPACES.split(raw);

		if (raws.length == 0) {
			return Set.of();
		}

		final Registry<T> theRegistry;
		{
			final Optional<Registry<T>> optional = access.lookup(registry);
			if (optional.isEmpty()) {
				return Set.of();
			}
			theRegistry = optional.get();
		}

		final Set<Holder<T>> holders = new HashSet<>(raws.length);

		for (final String str : raws) {
			if (str.indexOf('*') >= 0) {
				if (str.charAt(0) == '#') {
					streamTagEntries(theRegistry, new Glob(str.substring(1))).forEach(holders::add);
				} else {
					streamEntries(theRegistry, new Glob(str)).forEach(holders::add);
				}
			} else if (str.startsWith("#") && str.length() > 1) {
				final Identifier id = Identifier.tryParse(str.substring(1));
				if (id != null) {
					readTag(theRegistry, id).forEach(holders::add);
				}
			} else {
				final Identifier id = Identifier.tryParse(str);
				if (id != null) {
					theRegistry.get(id).ifPresent(holders::add);
				}
			}
		}

		return holders;
	}

	public static <T> Iterable<? extends Holder<T>> readTag(
		final Registry<T> registry,
		final Identifier tag
	) {
		return registry.getTagOrEmpty(TagKey.create(registry.key(), tag));
	}

	public static <T> Stream<? extends Holder<T>> streamTag(
		final Registry<T> registry,
		final Identifier tag
	) {
		return registry.get(TagKey.create(registry.key(), tag))
			.map(HolderSet::stream)
			.orElseGet(Stream::empty);
	}

	public static <T> Stream<? extends Holder<T>> streamEntries(
		final Registry<T> registry,
		final Glob glob
	) {
		return registry.listElements()
			.filter(glob::test);
	}

	public static <T> Stream<? extends Holder<T>> streamTagEntries(
		final Registry<T> registry,
		final Glob glob
	) {
		return registry.getTags()
			.filter(glob::test)
			.flatMap(HolderSet::stream);
	}

	public static <T> @Nullable T access(
		final RegistryAccess access,
		final ResourceKey<Registry<T>> registry,
		final Identifier id
	) {
		return access.get(ResourceKey.create(registry, id)).map(Holder::value).orElse(null);
	}
}
