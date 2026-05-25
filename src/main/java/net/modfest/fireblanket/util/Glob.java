package net.modfest.fireblanket.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A poor implementation of globs.
 *
 * @author Ampflower
 */
// TODO: this may make more sense to implement as regex later on.
@NullMarked
public record Glob(
	List<String> matchers,
	boolean wildcardStart,
	boolean wildcardEnd
) implements Predicate<String> {

	/**
	 * @see #Glob(String)
	 * @deprecated The canonical constructor is not stable API. It should not be relied upon.
	 */
	@ApiStatus.Experimental
	@Deprecated
	public Glob {
		for (final String value : matchers) {
			if (value.isEmpty()) {
				throw new IllegalArgumentException("not a valid matcher: " + matchers);
			}
		}

		matchers = List.copyOf(matchers);
	}

	public Glob(final String glob) {
		if (glob.isEmpty()) { // implicit NPE
			throw new IllegalArgumentException("Pass a pattern?");
		}

		final List<String> matchers = new ArrayList<>();
		boolean wildcardStart = false;
		boolean wildcardEnd = false;

		// Why not split?
		// "*meow" and "*meow*" are equivalent while "*" is an empty array and "" is an array of itself.
		// How does any of this make sense? I don't know!
		// So, we're just manually parsing the blob instead.
		// It's also not out of the realm of possibility that we might want to upgrade this later.
		int index, last = 0;

		while ((index = glob.indexOf('*', last)) >= 0) {
			if (index == 0) {
				wildcardStart = true;
			}
			if (index != last) {
				matchers.add(glob.substring(last, index));
			}
			last = indexOfNot(glob, '*', index);
			if (last - index > 1) {
				// What's a path? We're working with resource locations!
				// SPDX-Resource-Locations even :3
				// What? It's identifiers now? And they have path separators?
				throw new IllegalArgumentException("*".repeat(last - index) + " is unsupported.");
			}
		}

		if (last == glob.length()) {
			wildcardEnd = true;
		} else {
			matchers.add(glob.substring(last));
		}

		this(matchers, wildcardStart, wildcardEnd);
	}

	@Override
	public boolean test(final String s) {
		if (matchers.isEmpty()) {
			return wildcardStart | wildcardEnd;
		}

		final Iterator<String> itr = matchers.iterator();
		String value = itr.next();
		int index, last;

		if ((!wildcardStart && !s.startsWith(value)) || (index = s.indexOf(value)) < 0) {
			return false;
		}

		last = index + value.length();

		while (itr.hasNext()) {
			value = itr.next();

			if ((index = s.indexOf(value, last)) < 0) {
				return false;
			}

			last = index + value.length();
		}

		if (last != s.length()) {
			return wildcardEnd || s.endsWith(value);
		}

		return true;
	}

	public boolean test(final Identifier id) {
		return test(id.toString());
	}

	public boolean test(final ResourceKey<?> resourceKey) {
		return test(resourceKey.identifier());
	}

	public boolean test(final TagKey<?> key) {
		return test(key.location());
	}

	public boolean test(final HolderSet<?> set) {
		if (!set.isBound()) {
			return false;
		}
		final Optional<? extends TagKey<?>> optional = set.unwrapKey();
		return optional.isPresent() && test(optional.get());
	}

	public boolean test(Holder<?> holder) {
		if (!holder.isBound()) {
			return false;
		}
		final Optional<? extends ResourceKey<?>> optional = holder.unwrapKey();
		return optional.isPresent() && test(optional.get());
	}

	private static int indexOfNot(final String str, final char value, int index) {
		while (index < str.length() && str.charAt(index) == value) {
			index++;
		}

		return index;
	}
}
