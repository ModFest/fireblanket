package net.modfest.fireblanket.diagnostics;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import sun.misc.Unsafe;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.WeakReference;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A wrapper around {@link java.lang.LiveStackFrame} and {@link StackWalker} that
 * introspects the stack and recursively dumps all encountered objects.
 * <p>
 * Very minimal API surface is available, as this class is highly dangerous and
 * intentionally breaches the Java sandbox <em>as a fallback</em> for the cases
 * that allowing arbitrary module access cannot be done.
 * <p>
 * For cases where you only want to introspect into the stack,
 * use {@link #dumpStack(Object...)} as a simple yet very verbose call stack dumper.
 * <p>
 * For cases where you want to terminate the call site on error,
 * but have a complete, verbose, dump of the stack,
 * use {@link #dumpStackAndThrow(Supplier, Object...)} as a terminating call stack dumper.
 * <p>
 * If you want to dump any arbitrary object you have access to the logger,
 * use {@link #print(Object...)} to introspect into said objects.
 *
 * @author Ampflower
 **/
// We're linking to JVM internal classes in documentation. It's fine.
@SuppressWarnings("JavadocReference")
public final class ForbiddenStackWalker {
	private static final Logger logger = LogUtils.getLogger();

	private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

	@Nullable
	private static final Class<?> $LiveStackFrame;
	@Nullable
	private static final MethodHandle $getMonitors, $getLocals, $getStack;

	@Nullable
	private static final Class<?> $PrimitiveSlot;
	@Nullable
	private static final MethodHandle $size, $intValue, $longValue;


	private static final Set<Class<?>> $debugAnnotations;

	@Nullable
	private static final Class<? extends Annotation> $MixinMerged;
	@Nullable
	private static final MethodHandle $mixin;

	@Nullable
	private static final StackWalker forbidden;

	// FIXME: A WeakMap should be used here. Strongly retaining classes is a bad idea.
	private static final Map<Class<?>, Set<String>> mixined = new IdentityHashMap<>();

	private static final Set<StackWalker.Option> options = Set.of(
		StackWalker.Option.RETAIN_CLASS_REFERENCE,
		StackWalker.Option.SHOW_HIDDEN_FRAMES
	);

	@Nullable
	private static final Unsafe unsafe;
	private static final long address;

	private static final ThreadLocal<Object> crashTimeWitness = new ThreadLocal<>();
	private static final ThreadLocal<WeakReference<?>> lock = new ThreadLocal<>();

	// region Jail breaking & initialization
	static {
		Unsafe junsafe = null;
		long jaddress = -1L;
		try {
			final var funsafe = Unsafe.class.getDeclaredField("theUnsafe");

			funsafe.setAccessible(true);

			junsafe = (Unsafe) funsafe.get(null);

			// Most objects are typically not larger than 32 bytes,
			// but memory layout is not guaranteed between JVMs,
			// or even different runs.
			//
			// So, just check all 256 bytes around the object.
			final int rounds = 32;

			for (int i = 0; i < rounds; i += 8) {
				long a = junsafe.getLong(funsafe, i);
				funsafe.setAccessible(false);
				long b = junsafe.getLong(funsafe, i);
				funsafe.setAccessible(true);
				long ans = a ^ b;
				if (ans != 0L) {
					logger.info("found: {}", Long.toHexString(ans));

					if (Long.bitCount(ans) != 1) {
						logger.warn("Dirty. May produce unclean results.");
					}

					if (ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN) {
						ans = Long.reverseBytes(ans);
					}

					jaddress = Long.numberOfLeadingZeros(ans) / Long.BYTES + i;

					if (!junsafe.getBoolean(funsafe, jaddress)) {
						logger.error("Incorrect address: {} -> {}", jaddress, ans);
						throw new AssertionError();
					}

					funsafe.setAccessible(false);
					if (junsafe.getBoolean(funsafe, jaddress)) {
						logger.error("Incorrect address: {} -> {}", jaddress, ans);
						throw new AssertionError();
					}

					junsafe.putBoolean(funsafe, jaddress, true);
					//noinspection deprecation - no suitable replacement outside of try or die
					if (!funsafe.isAccessible()) {
						logger.error("Incorrect address: {} -> {}", jaddress, ans);
						throw new AssertionError();
					}

					break;
				}
			}

			if (jaddress == -1L) {
				throw new AssertionError("override not found, unfortunate");
			}
		} catch (Throwable t) {
			logger.warn("Trivial jailbreak failed, no funsafe accessor.", t);
		}
		logger.warn("AccessibleObject jailbroken. Beware.");
		unsafe = junsafe;
		address = jaddress;
	}

	/**
	 * Dangerous function: breaches the Java sandbox and forcefully sets
	 * the accessible flag to true.
	 *
	 * @param object The reflection object to jailbreak.
	 */
	@SuppressWarnings("deprecation") // no suitable replacement outside of try or die
	private static void jailbreak(AccessibleObject object) {
		if (unsafe != null && address != -1L) {
			unsafe.putBoolean(object, address, true);
		}
		try {
			if (!object.isAccessible()) {
				object.setAccessible(true);
			}
		} catch (Throwable t) {
			logger.warn("{} may not have been jailbroken:", object, t);
		}
	}

	/**
	 * Dangerous function: breaches the Java sandbox and forcefully sets
	 * the accessible flag to true.
	 *
	 * @param method The method to jailbreak.
	 * @return A jailbroken handle of the method.
	 */
	private static @Nullable MethodHandle jailbreakAsHandle(Method method) {
		jailbreak(method);
		try {
			return lookup.unreflect(method);
		} catch (ReflectiveOperationException roe) {
			logger.warn("Failed to unreflect {}", method, roe);
		}
		return null;
	}

	static { // LiveStackFrame
		StackWalker theForbiddenOne = null;
		Class<?> liveStackFrame = null;
		MethodHandle getMonitors = null, getLocals = null, getStack = null;
		try {
			Method getStackWalker = null;
			liveStackFrame = Class.forName("java.lang.LiveStackFrame");

			for (var m : liveStackFrame.getDeclaredMethods()) {
				switch (m.getName()) {
					case "getMonitors" -> {
						if (m.getParameterCount() == 0) {
							getMonitors = jailbreakAsHandle(m);
						}
					}
					case "getLocals" -> {
						if (m.getParameterCount() == 0) {
							getLocals = jailbreakAsHandle(m);
						}
					}
					case "getStack" -> {
						if (m.getParameterCount() == 0) {
							getStack = jailbreakAsHandle(m);
						}
					}
					case "getStackWalker" -> {
						if (m.getParameterCount() != 1) {
							continue;
						}
						if (!m.getParameterTypes()[0].isAssignableFrom(Set.class)) {
							continue;
						}
						jailbreak(m);
						getStackWalker = m;
					}
				}
			}

			if (getStackWalker != null) {
				theForbiddenOne = (StackWalker) getStackWalker.invoke(null, options);
			}
		} catch (Exception roe) {
			logger.error("The stack walkers sleep, for as we cannot reflect.", roe);
			logger.error(
				"Should you need the stack walkers to run, tell this very JVM \"--add-opens=java.base/java.lang=ALL-UNNAMED\"");
		}

		if (theForbiddenOne == null) {
			// Use the lesser walker.
			theForbiddenOne = StackWalker.getInstance(options);
		}

		$LiveStackFrame = liveStackFrame;
		$getMonitors = getMonitors;
		$getStack = getStack;
		$getLocals = getLocals;

		forbidden = theForbiddenOne;
	}

	static { // PrimitiveSlot
		Class<?> primitiveSlot = null;
		MethodHandle size = null, intValue = null, longValue = null;

		try {
			primitiveSlot = Class.forName("java.lang.LiveStackFrame$PrimitiveSlot");

			for (var m : primitiveSlot.getDeclaredMethods()) {
				switch (m.getName()) {
					case "size" -> {
						if (m.getParameterCount() == 0) {
							size = jailbreakAsHandle(m);
						}
					}
					case "intValue" -> {
						if (m.getParameterCount() == 0) {
							intValue = jailbreakAsHandle(m);
						}
					}
					case "longValue" -> {
						if (m.getParameterCount() == 0) {
							longValue = jailbreakAsHandle(m);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannot load ");
		}

		$PrimitiveSlot = primitiveSlot;
		$size = size;
		$intValue = intValue;
		$longValue = longValue;
	}

	/**
	 * Tests whether jailbreaking was possible.
	 *
	 * @throws IllegalStateException If jailbreaking failed.
	 */
	@SuppressWarnings("unused") // API
	public static void expectJailbreak() throws IllegalStateException {
		if (unsafe == null) {
			throw new IllegalStateException("Jailbreak failed.");
		}
	}
	// endregion

	// region Mixin init
	static { // Known Debug Annotations
		final var debugAnnotations = new HashSet<Class<?>>();
		appendIfNonNull(debugAnnotations, classOrNull("org.spongepowered.asm.mixin.Debug"));
		$debugAnnotations = Set.copyOf(debugAnnotations);
	}

	static { // MixinMerged
		$MixinMerged = classOrNull("org.spongepowered.asm.mixin.transformer.meta.MixinMerged", Annotation.class);
		MethodHandle mixin = MethodHandles.constant(String.class, "** Cannot fetch mixin **");
		try {
			final MethodHandle $intern = lookup.findVirtual(
				String.class,
				"intern",
				MethodType.methodType(String.class)
			);
			final MethodHandle $mixin = lookup.findVirtual($MixinMerged, "mixin", MethodType.methodType(String.class));
			mixin = MethodHandles.filterArguments($intern, 0, $mixin);
		} catch (Throwable t) {
			logger.warn("Cannot lookup MixinMerged: ", t);
		}
		$mixin = mixin;
	}
	// endregion

	// region Stack Frame Dumper

	/**
	 * Walks the stack, dumps every encountered frame, then throws the provided exception when finished.
	 * <p>
	 * All objects, including locals, stack operands and monitors,
	 * encountered will be recursively dumped as per {@link #print(Object...)}.
	 *
	 * @param throwable A throwable supplier
	 * @param context   The context for dumping the stack.
	 * @throws T The throwable the user wanted to throw for the reason provided.
	 * @see #dumpStack(Object...)
	 */
	@SuppressWarnings("unused") // API
	public static <T extends Throwable> void dumpStackAndThrow(
		final @Nullable Supplier<@Nullable T> throwable,
		final @Nullable Object @Nullable ... context
	) throws T {
		if (isAlreadyDumping()) {
			logger.warn("Terminating recursive call.");
			return;
		}

		dump(context);

		// If we didn't get an exception provider for some reason,
		// fall back to throwing an assertion error instead.
		if (throwable == null) {
			throw new AssertionError();
		}
		final var toThrow = throwable.get();
		if (toThrow == null) {
			throw new AssertionError();
		}
		throw toThrow;
	}

	/**
	 * Walks the stack and dumps every encountered frame.
	 * The caller will resume operation once the dump is completed.
	 * <p>
	 * All objects, including locals, stack operands and monitors,
	 * encountered will be recursively dumped as per {@link #print(Object...)}.
	 *
	 * @param context The context for dumping the stack. May include a reason.
	 * @see #dumpStackAndThrow(Supplier, Object...)
	 */
	@SuppressWarnings("unused") // API
	public static void dumpStack(final @Nullable Object @Nullable ... context) {
		if (isAlreadyDumping()) {
			logger.warn("Terminating recursive call.");
			return;
		}

		dump(context);
	}

	// Backing dump call
	private static void dump(final @Nullable Object @Nullable [] context) {
		logger.error("Dumping stack:");
		if (context != null) {
			print(context);
		}

		if (forbidden == null) {
			logger.error("No stack walker, resorting to throwable", new Throwable());
			return;
		}

		final Object object = new Object();

		try {
			lock.set(new WeakReference<>(object));

			forbidden.walk(stream -> {
				final var builder = new StringBuilder(1024);
				final var witness = newWitnessSet();

				stream.forEach(frame -> consume(frame, builder, witness));
				return null;
			});
		} finally {
			lock.remove();
		}
	}

	/**
	 * Tests whether the dumper is already active on the current thread.
	 * <p>
	 * Prevents recursion when the stackwalker encounters an iterable it's dumping.
	 */
	private static boolean isAlreadyDumping() {
		final WeakReference<?> reference = lock.get();

		return reference != null && reference.get() != null;
	}

	/**
	 * Dumps the {@link StackWalker.StackFrame} to the provided builder.
	 * <p>
	 * All objects, including locals, stack operands and monitors,
	 * encountered will be recursively dumped as per {@link #print(Object...)}.
	 *
	 * @param frame   The frame to dump. It may be a {@link java.lang.LiveStackFrame}.
	 * @param builder The builder to print to.
	 * @param witness The witness set to avoid reprinting large recursive toString and objects.
	 * @see #dumpStack(Object...)
	 * @see #dumpStackAndThrow(Supplier, Object...)
	 */
	private static void consume(
		final StackWalker.StackFrame frame,
		final StringBuilder builder,
		final Set<Object> witness
	) {
		builder.setLength(0);
		try {
			mixins:
			if (witness.add(frame.getDeclaringClass())) {
				Set<String> arr = getMixins(frame.getDeclaringClass());
				if (arr == null || arr.isEmpty()) {
					break mixins;
				}
				builder.append("\tMixins[").append(arr.size()).append("]:\n");
				for (var a : arr) {
					builder.append("\t\t- ").append(a).append('\n');
				}
			}
			if ($LiveStackFrame == null || !$LiveStackFrame.isInstance(frame)) {
				logger.trace("Not a live frame: {}", frame);
				return;
			}
			monitors:
			if ($getMonitors != null) {
				Object[] arr = (Object[]) $getMonitors.invoke(frame);
				if (arr == null || arr.length == 0) {
					break monitors;
				}
				print(builder.append("\tMonitors[").append(arr.length).append("]:\n"), 2, witness, arr);
			}
			locals:
			if ($getLocals != null) {
				Object[] arr = (Object[]) $getLocals.invoke(frame);
				if (arr == null || arr.length == 0) {
					break locals;
				}
				final var trimArr = trim(arr);
				print(builder.append("\tLocals[").append(arr.length).append("]:\n"), 2, witness, trimArr);
				if (trimArr != arr) {
					builder.append("\t\t... ").append(arr.length - trimArr.length).append(" more zeros\n");
				}
			}
			stack:
			if ($getStack != null) {
				Object[] arr = (Object[]) $getStack.invoke(frame);
				if (arr == null || arr.length == 0) {
					break stack;
				}
				print(builder.append("\tStack[").append(arr.length).append("]:\n"), 2, witness, arr);
			}
			logger.error(
				"at {}\n{}.{}{} @ LI:{} => BCI:{}\n{}",
				frame,
				frame.getDeclaringClass().getName(),
				frame.getMethodName(),
				frame.getMethodType(),
				frame.getLineNumber(),
				frame.getByteCodeIndex(),
				builder
			);
		} catch (Throwable e) {
			final var crasher = crashTimeWitness.get();
			logger.error(
				"at {}\n{}.{}{} @ LI:{} => BCI:{}\n{}\nTerminated early: Failed to walk the frame.\n\tWitness object: {}@{}",
				frame,
				frame.getDeclaringClass().getName(),
				frame.getMethodName(),
				frame.getMethodType(),
				frame.getLineNumber(),
				frame.getByteCodeIndex(),
				builder,
				crasher == null ? "null" : crasher.getClass().getName(),
				System.identityHashCode(crasher),
				e
			);
			for (final var entry : witness) {
				logger.error("Encountered {}@{}", entry.getClass().getName(), System.identityHashCode(entry));
			}
		}
	}

	/**
	 * Trims the provided array of trailing primitive zeros.
	 *
	 * @param objects The array to trim.
	 * @return The trimmed array.
	 */
	private static Object[] trim(final Object... objects) {
		int i = objects.length;
		while (i > 0 && isZero(objects[i - 1])) {
			i--;
		}

		if (i == objects.length) {
			return objects;
		}

		return Arrays.copyOf(objects, i);
	}
	// endregion

	// region Object Dumper

	/**
	 * Prints all provided objects, including nulls, to the logger.
	 * <p>
	 * This will attempt to recursively dump all arrays, {@link Iterable iterables},
	 * {@link Map maps} and {@link Throwable throwables} given and encountered.
	 * <p>
	 * To avoid recursion and large logs, a witness set will prevent dumping the same object over and over.
	 * <p>
	 * If you are debugging from an arbitrary point on the stack,
	 * call {@link #dumpStack(Object...)} instead.
	 *
	 * @param objects All objects to introspect and observe.
	 * @apiNote Due to this function's ability to breach the sandbox,
	 * 	the objects dumped will only be printed directly to the logger.
	 */
	@SuppressWarnings("unused") // API
	public static void print(
		final @Nullable Object @NotNull ... objects
	) {
		final StringBuilder builder = new StringBuilder();
		print(builder, 0, newWitnessSet(), objects);
		logger.error("Object dump:\n{}", builder);
	}

	/**
	 * Prints all encountered objects to the provided {@link StringBuilder}.
	 *
	 * @param builder The target string builder.
	 * @param depth   The tab depth. Used and incremented recursively to indent.
	 * @param witness An IdentitySet. See {@link #newWitnessSet()}
	 * @param objects All objects to introspect and observe.
	 * @see #dumpStack(Object...)
	 * @see #dumpStackAndThrow(Supplier, Object...)
	 * @see #print(Object...)
	 */
	private static void print(
		final StringBuilder builder,
		final int depth,
		final Set<Object> witness,
		final Object... objects
	) {
		for (final var object : objects) {
			print(builder, depth, witness, object);
		}
	}

	/**
	 * Prints all encountered objects to the provided {@link StringBuilder}.
	 *
	 * @param builder The target string builder.
	 * @param depth   The tab depth. Used and incremented recursively to indent.
	 * @param witness An IdentitySet. See {@link #newWitnessSet()}
	 * @param object  The object to introspect and observe.
	 * @see #dumpStack(Object...)
	 * @see #dumpStackAndThrow(Supplier, Object...)
	 * @see #print(Object...)
	 */
	private static void print(
		final StringBuilder builder,
		int depth,
		final Set<Object> witness,
		final @Nullable Object object
	) {
		builder.repeat('\t', depth++).append("- ");

		final var number = primitiveAsBox(object);

		if (number != null) {
			builder.append(number).append('\n');
			return;
		}

		boolean newObject = witness.add(object);
		if (newObject) {
			if (object instanceof final Object[] array) {
				printIterableIdentity(builder, object).append('\n');
				for (final var entry : array) {
					print(builder, depth, witness, entry);
				}
				return;
			}

			// sun.nio.fs.UnixPath will indefinitely recurse
			if (!(object instanceof Path) && object instanceof final Iterable<?> iterable) {
				printIterableIdentity(builder, object).append('\n');
				if (depth > 32) {
					logger.warn("Depth exceeded: {}@{}", object.getClass().getName(), Integer.toHexString(System.identityHashCode(object)));
					return;
				}
				for (final var entry : iterable) {
					print(builder, depth, witness, entry);
				}
				return;
			}

			if (object instanceof final Map<?, ?> map) {
				printIdentity(builder, object).append('\n');
				for (final var entry : map.entrySet()) {
					print(builder, depth, witness, entry);
				}
				return;
			}

			if (object instanceof final Throwable throwable) {
				printIdentity(builder, object).append('\n');

				final var tabCache = "\t".repeat(depth);
				print(builder, tabCache, throwable);

//				for (final var trace : throwable.getStackTrace()) {
//					builder.append(tabCache).append(trace).append('\n');
//				}
				return;
			}
		}

		printObject(builder, depth, object, newObject).append('\n');
	}

	/**
	 * Prints the identity of the iterable.
	 * <p>
	 * The format is of {@code class[count][]@4454[4454]}
	 *
	 * @param builder The builder to print the identity to.
	 * @param object  The object to print the identity of.
	 * @return The builder, for ease of chaining.
	 * @see #print(StringBuilder, int, Set, Object)
	 * @see #printObject(StringBuilder, int, Object, boolean)
	 */
	private static StringBuilder printIterableIdentity(
		final StringBuilder builder,
		final @NotNull Object object
	) {
		final var base = arrayDimensions(object);

		builder.append(base.component().getName())
			.append('[');

		if (object instanceof final Object[] array) {
			builder.append(array.length);
		} else if (object instanceof final Collection<?> collection) {
			try {
				builder.append(collection.size());
			} catch (Throwable t) {
				printUntilFilter(builder, "", ForbiddenStackWalker.class, t);
			}
		} else {
			try {
				final var optional = sizeOf(object);
				optional.ifPresentOrElse(builder::append, () -> builder.append("** UNKNOWN **"));
			} catch (Throwable t) {
				printUntilFilter(builder, "", ForbiddenStackWalker.class, t);
			}
		}

		builder.append(']')
			.repeat("[]", Math.max(base.dimensions() - 1, 0))
			.append('@')
			.append(Integer.toHexString(System.identityHashCode(object)));

		return printCustomHashCode(builder, object);
	}

	/**
	 * Prints the identity of the given object.
	 * <p>
	 * The output would be as if the target {@code .toString()} was never overridden.
	 * <p>
	 * If the class of the object defines a custom {@code .hashCode()} function,
	 * that will be called and appended as brackets.
	 * <p>
	 * Should {@code .hashCode()} fail,
	 * the resulting exception will be printed in the brackets instead,
	 * with the stack trace truncated to this class.
	 *
	 * @param builder The builder to print the identity to.
	 * @param object  The object to print the identity of. Must not be null.
	 * @return The builder, for ease of chaining.
	 * @see #print(StringBuilder, int, Set, Object)
	 * @see #printObject(StringBuilder, int, Object, boolean)
	 */
	private static StringBuilder printIdentity(
		final StringBuilder builder,
		final @NotNull Object object
	) {
		builder.append(object.getClass().getName())
			.append("@")
			.append(Integer.toHexString(System.identityHashCode(object)));

		return printCustomHashCode(builder, object);
	}

	/**
	 * If the class of the given object defines a custom {@code .hashCode()} function,
	 * prints it in the provided builder, surrounded in brackets.
	 * <p>
	 * Should {@code .hashCode()} fail,
	 * the resulting exception will be printed in the brackets instead,
	 * with the stack trace truncated to this class.
	 *
	 * @param builder The builder to print the hash code to.
	 * @param object  The object to print the hash code of. May be null.
	 * @return The builder, for ease of chaining.
	 * @see #printIdentity(StringBuilder, Object)
	 * @see #printIterableIdentity(StringBuilder, Object)
	 */
	private static StringBuilder printCustomHashCode(
		final StringBuilder builder,
		final @Nullable Object object
	) {
		if (hasCustomHashCode(object)) {
			builder.append('[');
			try {
				builder.append(Integer.toHexString(object.hashCode()));
			} catch (Throwable t) {
				printUntilFilter(
					builder.append("\n.hashCode() crashed: "),
					"",
					ForbiddenStackWalker.class,
					t
				);
			}
			builder.append(']');
		}

		return builder;
	}

	/**
	 * Prints the identity of the given object, then its toString if it has any.
	 * <p>
	 * The identity will be printed by {@link #printIdentity(StringBuilder, Object)}.
	 * <p>
	 * If the class of the object defines a custom {@code .toString()} function,
	 * that will be called and appended after the object.
	 * <p>
	 * Should {@code .toString()} fail,
	 * the resulting exception will be printed after the object instead,
	 * with the stack trace truncated to this class.
	 *
	 * @param builder The builder to print the object to.
	 * @param depth   The tab depth.
	 * @param object  The object to print the identity and value of. May be null.
	 * @param witness The witness set to avoid reprinting recursive and large toStrings.
	 * @return The builder, for ease of chaining.
	 * @see #print(StringBuilder, int, Set, Object)
	 * @see #printIdentity(StringBuilder, Object)
	 */
	private static StringBuilder printObject(
		final StringBuilder builder,
		final int depth,
		final @Nullable Object object,
		final boolean newObject
	) {
		if (object == null) {
			builder.append("** NULL **");
			return builder;
		}

		printIdentity(builder, object);

		if (hasCustomToString(object)) {
			final var tabCache = "\t".repeat(depth);
			builder.append('\n');
			try {
				final var lines = object.toString().lines().iterator();

				if (!newObject && lines.hasNext()) {
					return builder.append(tabCache)
						.append(lines.next())
						.append(lines.hasNext() ? "...\n" : "\n");
				}

				while (lines.hasNext()) {
					builder.append(tabCache).append(lines.next()).append('\n');
				}
			} catch (Throwable t) {
				printUntilFilter(
					builder.append("\n.toString() crashed: "),
					tabCache,
					ForbiddenStackWalker.class,
					t
				);
			}
		}
		return builder;
	}
	// endregion

	// region Mixins

	/**
	 * Fetches mixins in a given class.
	 * <p>
	 * The return set may be empty, indicating no mixins were found.
	 *
	 * @param clazz The class to lookup mixins in.
	 * @return Mixins found.
	 * @implNote If it is the first time the class is looked up,
	 * 	and there is a debug annotation, or other problematic annotations present,
	 * 	a warning will be issued in the log.
	 */
	public static Set<String> getMixins(Class<?> clazz) {
		return mixined.computeIfAbsent(clazz, ForbiddenStackWalker::getMixinsInternal);
	}

	private static Set<String> getMixinsInternal(Class<?> clazz) {
		for (final var annotation : clazz.getAnnotations()) {
			// Issue warnings upon encountering @Debug.
			// Usually, this is a `@Debug(export = true)` left by accident.
			if ($debugAnnotations.contains(annotation.getClass())) {
				logger.warn("Debug dump present: {} => {}", clazz, annotation);
			}
		}

		if ($MixinMerged == null || $mixin == null) {
			return Set.of();
		}

		final var set = new HashSet<String>();
		filter(clazz.getDeclaredFields(), $MixinMerged, $mixin, set);
		filter(clazz.getDeclaredMethods(), $MixinMerged, $mixin, set);
		return Set.copyOf(set);
	}

	/**
	 * Finds elements marked with a given annotation,
	 * then invokes the handle with the annotation instance.
	 *
	 * @param elements   The array of elements to find annotations on.
	 * @param annotation The annotation to find.
	 * @param handle     The MethodHandle that operates on the annotation.
	 *                   It must accept the annotation as its sole argument and must return a string.
	 * @param set        The set to dump to.
	 */
	private static void filter(
		final AnnotatedElement[] elements,
		final @Nullable Class<? extends Annotation> annotation,
		final @Nullable MethodHandle handle,
		final Set<@NotNull String> set
	) {
		if (annotation == null || handle == null) {
			return;
		}
		for (final var element : elements) {
			final var a = element.getAnnotation(annotation);
			if (a == null) {
				continue;
			}
			try {
				final var str = (String) handle.invoke(a);
				// Null should be impossible, but we don't know what the handle could return.
				if (str != null) {
					set.add(str);
				}
			} catch (Throwable t) {
				logger.warn("Cannot lookup {} -> {}:", element, a, t);
				// Bad practice but if it's the handle that's broken then, we can't do much.
				break;
			}
		}
	}
	// endregion

	// region Primitives

	/**
	 * Tests if the provided object is a {@link java.lang.LiveStackFrame.PrimitiveSlot},
	 * and is either 0 or uninitialized.
	 *
	 * @param probablePrimitive The object to test.
	 * @return If the provided object is a primitive 0.
	 */
	private static boolean isZero(final @Nullable Object probablePrimitive) {
		meow:
		try {
			if (
				$PrimitiveSlot == null ||
					$size == null ||
					!$PrimitiveSlot.isInstance(probablePrimitive)
			) {
				break meow;
			}
			switch ((int) $size.invoke(probablePrimitive)) {
				case 4 -> {
					if ($intValue != null && (int) $intValue.invoke(probablePrimitive) == 0) {
						return true;
					}
				}
				case 8 -> {
					if ($longValue != null && (long) $longValue.invoke(probablePrimitive) == 0L) {
						return true;
					}
				}
			}
		} catch (Throwable ignored) {
		}
		return false;
	}

	/**
	 * If the provided object is a {@link java.lang.LiveStackFrame.PrimitiveSlot},
	 * return a boxed value of the primitive, otherwise null.
	 * <p>
	 * Note: due to current limitations, this cannot tell if the primitive is a {@code boolean},
	 * {@code byte}, {@code short}, {@code char}, {@code int}, {@code long},
	 * {@code float} or {@code double}.
	 * <p>
	 * If the local variable table was removed or never emitted,
	 * or we are reading the stack, it would require reading the raw bytecode
	 * to figure out what the value is supposed to be, which may be impossible
	 * for primitives smaller than {@code int} due to type erasure.
	 *
	 * @param probablePrimitive The object to re-box as a {@link Number}.
	 * @return A boxed number if the provided object is a primitive, null otherwise.
	 */
	private static @Nullable Number primitiveAsBox(final @Nullable Object probablePrimitive) {
		meow:
		try {
			if (
				$PrimitiveSlot == null ||
					$size == null ||
					!$PrimitiveSlot.isInstance(probablePrimitive)
			) {
				break meow;
			}
			switch ((int) $size.invoke(probablePrimitive)) {
				case 4 -> {
					if ($intValue != null) {
						return (int) $intValue.invoke(probablePrimitive);
					}
				}
				case 8 -> {
					if ($longValue != null) {
						return (long) $longValue.invoke(probablePrimitive);
					}
				}
			}
		} catch (Throwable ignored) {
		}
		return null;
	}
	// endregion

	// region Reflective Introspection

	/**
	 * Tests whether the given object has implemented {@link #toString()}.
	 */
	private static boolean hasCustomToString(final @Nullable Object object) {
		if (object == null) {
			return false;
		}
		try {
			return object.getClass().getMethod("toString").getDeclaringClass() != Object.class;
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * Tests whether the given object has implemented {@link #hashCode()}.
	 */
	private static boolean hasCustomHashCode(final @Nullable Object object) {
		if (object == null) {
			return false;
		}
		try {
			return object.getClass().getMethod("hashCode").getDeclaringClass() != Object.class;
		} catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * If the object is an array, finds the root component and how many dimensions the array has.
	 *
	 * @param object The array to find how many dimensions it has.
	 * @return {@link MultiDimArray} packaging the array class, root component class and dimensions.
	 */
	private static MultiDimArray<?> arrayDimensions(final Object object) {
		int i = 0;
		Class<?> clazz = object.getClass();
		while (clazz.isArray()) {
			i++;
			clazz = clazz.getComponentType();
		}
		return new MultiDimArray<>(object.getClass(), clazz, i);
	}

	/**
	 * Representation of an array with n-dimensions with an easily accessible component.
	 *
	 * @param <T>        The type of component.
	 * @param array      The array class of `n` dimensions and the root component.
	 * @param component  The root component of the array.
	 * @param dimensions How many dimensions the array has.
	 */
	private record MultiDimArray<T>(Class<?> array, Class<T> component, int dimensions) {
	}

	/**
	 * Returns the size of a given collection.
	 *
	 * @param object The potential collection.
	 * @return The size of the collection, if it is one.
	 * @throws Throwable If the {@code .size()} function throws.
	 */
	private static OptionalInt sizeOf(final @Nullable Object object) throws Throwable {
		if (object == null) {
			return OptionalInt.empty();
		}
		final MethodHandle size;
		try {
			size = lookup.findVirtual(object.getClass(), "size", MethodType.methodType(int.class));
		} catch (NoSuchMethodException | IllegalAccessException r) {
			return OptionalInt.empty();
		}
		return OptionalInt.of((int) size.invoke(object));
	}

	// endregion

	// region Throwables

	/**
	 * Recursively prints a throwable to the given builder.
	 *
	 * @param builder   The builder to print to.
	 * @param tab       Cached tab. Recursion of suppressed throwables appends a tab.
	 * @param throwable The throwable to print to the builder.
	 */
	private static void print(
		final StringBuilder builder,
		final String tab,
		final Throwable throwable
	) {
		printUntilFilter(builder, tab, null, throwable);
	}

	/**
	 * Recursively prints a throwable to the given builder.
	 *
	 * @param builder   The builder to print to.
	 * @param tab       Cached tab. Recursion of suppressed throwables appends a tab.
	 * @param filter    The root class to filter to. May be null.
	 * @param throwable The throwable to print to the builder.
	 */
	private static void printUntilFilter(
		final StringBuilder builder,
		final String tab,
		final @Nullable Class<?> filter,
		final Throwable throwable
	) {
		printUntilFilter(
			builder,
			tab,
			filter != null ? filter.getName() : null,
			throwable,
			newWitnessSet(),
			null
		);
	}

	/**
	 * Recursively prints a throwable to the given builder.
	 *
	 * @param builder   The builder to print to.
	 * @param tab       Cached tab. Recursion of suppressed throwables appends a tab.
	 * @param filter    The root class to filter to. May be null.
	 * @param throwable The throwable to print to the builder.
	 * @param witness   The witness set.
	 * @param last      The last {@link StackTraceElement[]}. May be null.
	 */
	private static void printUntilFilter(
		final StringBuilder builder,
		final String tab,
		final @Nullable String filter,
		final Throwable throwable,
		final Set<Throwable> witness,
		final @Nullable StackTraceElement[] last
	) {
		builder.append(throwable).append('\n');

		if (!witness.add(throwable)) {
			return;
		}

		final var trace = throwable.getStackTrace();

		if (trace.length == 0) {
			return;
		}

		final var atTab = tab + "\tat ";

		int i = 0, l = trace.length - same(trace, last);
		for (; i < l; i++) {
			if (trace[i].getClassName().equals(filter)) {
				break;
			}
			builder.append(atTab).append(trace[i]).append('\n');
		}
		builder.append(tab).append("... ").append(trace.length - i).append(" more frames\n");

		for (final var suppressed : throwable.getSuppressed()) {
			printUntilFilter(builder, tab + "\t", filter, suppressed, witness, trace);
		}

		final var cause = throwable.getCause();
		if (cause != null && cause != throwable) {
			printUntilFilter(builder.append("Caused by: "), tab, filter, cause, witness, trace);
		}
	}

	private static int same(
		final @NotNull StackTraceElement[] curr,
		final @Nullable StackTraceElement[] last
	) {
		if (last == null) {
			return 0;
		}
		final int l = Math.min(curr.length, last.length);
		for (int i = 0; i < l; i++) {
			int j = i + 1;
			if (!curr[l - j].equals(last[l - j])) {
				return i;
			}
		}
		return l;
	}
	// endregion

	private static <T> Set<T> newWitnessSet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

	private static <T> void appendIfNonNull(final Set<@NotNull T> set, final @Nullable T t) {
		if (t != null) {
			set.add(t);
		}
	}

	private static @Nullable Class<?> classOrNull(final String name) {
		return classOrNull(name, Object.class);
	}

	private static @Nullable <T> Class<? extends T> classOrNull(final String name, final Class<T> base) {
		try {
			final var found = Class.forName(name);
			if (base.isAssignableFrom(base)) {
				return (Class<T>) found;
			}
		} catch (ClassNotFoundException ignored) {
		}
		return null;
	}
}

// Now, you're probably thinking to yourself: "Did she really wrote a thousand line stack dumper?"
// Yes, yes, I did. And you're probably also wondering: "Did she really wrote this for a one-off issue?"
// No. I've started this back in September 5th, 2025 and built it up until November 16th.
//
// Fun fact: It was originally made because I had to diagnose the DataFixerUpper.
