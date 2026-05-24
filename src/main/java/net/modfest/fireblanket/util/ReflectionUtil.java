package net.modfest.fireblanket.util;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.IdentityHashMap;

/**
 * @author Ampflower
 * @apiNote Consumers using this to fetch anything must take care that the ABI is not broken between versions.
 * 	This does not include any tooling to deal with mappings for you.
 */
@NullMarked
public final class ReflectionUtil {
	private static final Logger logger = LogUtils.getLogger();

	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	private static final MethodHandle nil = MethodHandles.empty(MethodType.genericMethodType(1));

	/**
	 * Mask to find the nest host field, requiring a non-static & synthetic field.
	 * <p>
	 * Note: This is explicitly ignoring access levels and whether final,
	 * as access wideners may ignore this expectation.
	 */
	private static final int RELEVANT_NEST_MASK = Opcodes.ACC_SYNTHETIC | Opcodes.ACC_STATIC;

	private static final IdentityHashMap<Class<?>, MethodHandle> hostFields = new IdentityHashMap<>();

	/**
	 * Expensive field search, you should only run this once per class
	 */
	private static MethodHandle search(Class<?> clazz) {
		// We can't unnest a static class.
		if (Modifier.isStatic(clazz.getModifiers())) {
			return nil;
		}

		final Class<?> host = clazz.getNestHost();

		// Either we have self, or the JVM is broken.
		// Or, we have an interface, which apparently doesn't implicitly mark its nested classes
		// as static anymore. When did that change? Or was I just oblivious to it?
		if (host == clazz || host == null || Modifier.isInterface(host.getModifiers())) {
			return nil;
		}

		for (final Field field : clazz.getDeclaredFields()) {
			if ((field.getModifiers() & RELEVANT_NEST_MASK) != Opcodes.ACC_SYNTHETIC) {
				continue;
			}
			if (field.getType() != host) {
				continue;
			}
			if (!field.trySetAccessible()) {
				continue;
			}
			try {
				return lookup.unreflectGetter(field);
			} catch (IllegalAccessException e) {
				return rethrowHandle(e, field.getType(), field.getDeclaringClass());
			}
		}

		logger.warn("I couldn't find anything in {}, can you? {}", clazz, Arrays.toString(clazz.getDeclaredFields()));

		return nil;
	}

	/**
	 * Produces a handle that always rethrows the provided exception.
	 */
	public static MethodHandle rethrowHandle(
		final Throwable exception,
		final Class<?> facade,
		final Class<?>... drop
	) {
		// Ensure the stack trace is initialized before forwarding it.
		exception.fillInStackTrace();

		return MethodHandles.dropArguments(
			MethodHandles.throwException(facade, exception.getClass()).bindTo(exception),
			0,
			drop
		);
	}

	/**
	 * Finds the host of the given object if the host
	 *
	 * @param object The object to get the host from.
	 * @return The host object, if the input object was an instanced nested class.
	 * @throws IllegalAccessException If attempting to access a class or field disallowed by Jigsaw.
	 * @apiNote Consumers using this to fetch super-classes must take care that the ABI is not broken between versions.
	 */
	public static @Nullable Object getHost(
		final @Nullable Object object
	) throws IllegalAccessException {
		if (object == null) {
			return null;
		}

		try {
			return hostFields.computeIfAbsent(object.getClass(), ReflectionUtil::search).invoke(object);
		} catch (IllegalAccessException e) {
			throw e;
		} catch (Throwable e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Finds the host of the given object, or the object itself if it is exactly the given host.
	 *
	 * @param <T>    The type of class. Warning: consumers may incur a {@link ClassCastException} if not careful.
	 * @param object The object to get the host from.
	 * @param host   The expected host type.
	 * @return An instance of host, if found and the object was a nested class of host.
	 * @throws IllegalAccessException If attempting to access a class or field disallowed by Jigsaw.
	 * @apiNote Consumers using this to fetch super-classes must take care that the ABI is not broken between versions.
	 */
	public static <T> @Nullable T getHostStrict(
		final @Nullable Object object,
		final @NotNull Class<T> host
	) throws IllegalAccessException {
		// We can't operate on a null.
		if (object == null) {
			return null;
		}

		// Short circuit
		if (object.getClass() == host) {
			return host.cast(object);
		}

		// It's not really unnestable now if we can't pass this check, is it?
		if (object.getClass().getNestHost() != host) {
			return null;
		}

		return host.cast(getHost(object));
	}

	/**
	 * Finds the host of the given object, or the object itself if it is exactly the given host.
	 *
	 * @param <T>    The type of class. Warning: consumers may incur a {@link ClassCastException} if not careful.
	 * @param object The object to get the host from.
	 * @param host   The expected host type.
	 * @return An instance of host, if found and the object was a nested class assignable to host.
	 * @throws IllegalAccessException If attempting to access a class or field disallowed by Jigsaw.
	 * @apiNote Consumers using this to fetch super-classes must take care that the ABI is not broken between versions.
	 */
	public static <T> @Nullable T getHost(
		final @Nullable Object object,
		final @NotNull Class<T> host
	) throws IllegalAccessException {
		// We can't operate on a null.
		if (object == null) {
			return null;
		}

		// Short circuit
		if (object.getClass() == host) {
			return host.cast(object);
		}

		// It's not really unnestable now if we can't pass this check, is it?
		if (!host.isAssignableFrom(object.getClass().getNestHost())) {
			return null;
		}

		return host.cast(getHost(object));
	}
}
