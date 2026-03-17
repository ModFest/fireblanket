package net.modfest.fireblanket.stacksmash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ampflower
 **/
public final class StackUtil {
	private static final Logger logger = LogUtils.getLogger();

	/**
	 * Retaining class references is necessary for the in-depth testing by {@link TraceLevel}.
	 */
	private static final StackWalker walker = StackWalker.getInstance(Set.of(
		StackWalker.Option.RETAIN_CLASS_REFERENCE,
		// Fun fact: it's more expensive to leave this out.
		// As this is directly in line of performance critical code (rendering),
		// it would be best to take as little of a hit as possible.
		StackWalker.Option.SHOW_REFLECT_FRAMES
	));

	/**
	 * Reference to the platform classloader, in the event it actually exists.
	 */
	private static final ClassLoader platform = Object.class.getClassLoader();

	/**
	 * Mod classes that are known to wrap push/pop.
	 **/
	private static final Set<String> knownSkippableClassNames = Set.of(
		"com.unascribed.ears.common.render.AbstractEarsRenderDelegate"
	);

	/**
	 * Vanilla or Fireblanket classes that are known to wrap push/pop.
	 */
	private static final Set<Class<?>> knownSkippableClasses = Set.of(
		PoseStack.class
	);

	/**
	 * Loaded classes that are known to wrap push/pop.
	 * <p>
	 * Note: You may want to use {@link TraceLevel#FUNCTION} to find these cases.
	 */
	private static final Set<Class<?>> skippableDelegates = new HashSet<>(knownSkippableClasses);

	@NotNull
	private static TraceLevel traceLevel = TraceLevel.byName(FireblanketConfig.get(ConfigSpecs.TRACE_LEVEL));

	static {
		final ClassLoader loader = StackUtil.class.getClassLoader();
		for (final String potentialClass : knownSkippableClassNames) {
			try {
				final Class<?> clazz = loader.loadClass(potentialClass);
				skippableDelegates.add(clazz);
			} catch (ClassNotFoundException | Error e) {
				logger.debug("{} not found", potentialClass, e);
			}
		}
	}

	/**
	 * Fetches the stack frame of the caller, skipping over
	 * {@link #knownSkippableClasses known skippable classes}.
	 *
	 * @param depth The amount necessary to skip.
	 *              If getting your direct caller, set it to 0.
	 *              If you're proxying, add 1 per each layer.
	 * @return The caller, either yours or your proxy's.
	 * @implNote This function ignores all Java-provided or platform-loaded classes,
	 * 	due to an optimization to avoid an expensive {@link String#startsWith(String)} call.
	 */
	@CheckReturnValue
	static StackWalker.StackFrame getCaller(final int depth) {
		// walker + self + caller to be skipped
		final int lambdaDepth = depth + 3;
		return walker.walk(stream -> stream
			.skip(lambdaDepth)
			.dropWhile(frame -> isSkippableClass(frame.getDeclaringClass()))
			.findFirst()
		).orElseThrow();
	}

	static boolean isSkippableClass(final Class<?> clazz) {
		// If it came from Java, we don't care about it.
		// Usually, this means it was a reflection frame.
		if (clazz.getClassLoader() == platform) {
			return true;
		}
		return skippableDelegates.contains(clazz);
	}

	public static void setTraceLevel(final @NotNull TraceLevel traceLevel) {
		StackUtil.traceLevel = Objects.requireNonNull(traceLevel, "traceLevel");

		// TODO: determine if there's any permanently retained MatrixStacks
	}

	public static @NotNull TraceLevel getTraceLevel() {
		return traceLevel;
	}
}
