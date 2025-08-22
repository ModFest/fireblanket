package net.modfest.fireblanket.stacksmash;

import com.mojang.logging.LogUtils;
import net.minecraft.util.StringIdentifiable;
import org.slf4j.Logger;

import java.lang.StackWalker.StackFrame;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ampflower
 **/
public enum TraceLevel implements StringIdentifiable {
	/**
	 * Disabled, no-op.
	 */
	NONE() {
		/* Intentionally overwritten as the parent logic is illsuited. */
		@Override
		public void logIfMismatched(final StackFrame trace, final StackFrame current) {
			logger.warn("{} missed the memo and called logIfMismatched erroneously. However, if it is intended, the traced frame is {}", current, trace, new Throwable());
		}

		@Override
		String check(final StackFrame trace, final StackFrame current) {
			if (trace != null) {
				return current + " found a " + trace + " lying around.";
			}
			return "find all the bugs";
		}
	},

	/**
	 * Matches pushes & pops at the module level.
	 */
	MOD() {
		@Override
		String check(final StackFrame trace, final StackFrame current) {
			final String classCheck = CLASS.check(trace, current);
			if (classCheck == null) {
				return null;
			}

			final Class<?> traceClass = trace.getDeclaringClass();
			final Class<?> currentClass = current.getDeclaringClass();

			final ClassLoader traceLoader = traceClass.getClassLoader();
			final ClassLoader currentLoader = currentClass.getClassLoader();

			if (traceLoader != currentLoader) {
				return String.format("Loader mismatch: %s vs. %s", traceLoader, currentLoader);
			}

			final CodeSource traceSource = traceClass.getProtectionDomain().getCodeSource();
			final CodeSource currentSource = currentClass.getProtectionDomain().getCodeSource();

			if (traceSource == currentSource) {
				return null;
			}

			if (traceSource == null || currentSource == null || !traceSource.implies(currentSource)) {
				return String.format("Code source mismatch: %s vs. %s", traceSource, currentSource);
			}

			return null;
		}
	},

	/**
	 * Matches pushes & pops at the package level.
	 */
	PACKAGE() {
		@Override
		String check(final StackFrame trace, final StackFrame current) {
			if (trace.getDeclaringClass().getPackage() != current.getDeclaringClass().getPackage()) {
				return String.format(
					"Package mismatch: %s vs. %s",
					trace.getDeclaringClass().getSimpleName(),
					current.getDeclaringClass().getSimpleName()
				);
			}
			return null;
		}
	},

	/**
	 * Matches pushes & pops at the class level.
	 */
	CLASS() {
		@Override
		String check(final StackFrame trace, final StackFrame current) {

			if (trace.getDeclaringClass() != current.getDeclaringClass()) {
				return String.format(
					"Class mismatch: %s vs. %s",
					trace.getDeclaringClass().getSimpleName(),
					current.getDeclaringClass().getSimpleName()
				);
			}
			return null;
		}
	},

	/**
	 * Matches pushes & pops at the function level.
	 */
	FUNCTION() {
		@Override
		String check(final StackFrame trace, final StackFrame current) {
			final String classCheck = CLASS.check(trace, current);
			if (classCheck != null) {
				return classCheck;
			}

			final TraceElement traceElement = TraceCache.toElement(trace);
			final TraceElement currentElement = TraceCache.toElement(current);
			if (!traceElement.isSameFunction(currentElement)) {
				return String.format("Method signature mismatch:\n\t%s\n\t%s", traceElement, currentElement);
			}
			/*
			if (!trace.getMethodName().equals(current.getMethodName())) {
				return mismatch(trace, current);
			}
			if (!trace.getDescriptor().equals(current.getDescriptor())) {
				return mismatch(trace, current);
			}
			*/
			return null;
		}

		private static String mismatch(final StackFrame trace, final StackFrame current) {
			return String.format(
				"Method signature mismatch: %s vs. %s",
				signature(trace),
				signature(current)
			);
		}

		private static String signature(final StackFrame frame) {
			return frame.getMethodName() + frame.getMethodType();
		}
	},
	;
	static final Logger logger = LogUtils.getLogger();

	private static final Map<String, TraceLevel> toLevel;

	private final String brigadierName = name().toLowerCase(Locale.ROOT);

	static {
		final Map<String, TraceLevel> traceLevelMap = new HashMap<>();

		for (final TraceLevel level : values()) {
			traceLevelMap.put(level.brigadierName, level);
		}

		toLevel = Map.copyOf(traceLevelMap);
	}

	public void logIfMismatched(final StackFrame trace, final StackFrame current) {
		if (trace == null) {
			logger.warn("Trace is null @ {}; your stack is corrupted.", current);
			return;
		}

		final String check = this.check(trace, current);
		if (check != null) {
			logger.warn("Your stack was smashed!\n{} != {}\nHigh risk of crashing: {}\n", trace, current, check, new StackSmashException(trace, current));
		}
	}

	@Override
	public String asString() {
		return brigadierName;
	}

	abstract String check(final StackFrame trace, final StackFrame current);

	static TraceLevel byName(final String name) {
		return toLevel.getOrDefault(name.toLowerCase(Locale.ROOT), NONE);
	}
}
