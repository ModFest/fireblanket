package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.NotNull;

/**
 * Tracing utilities that when called can start and stop traces.
 *
 * @author Ampflower
 * @see StackTracer
 **/
public interface Tracer {
	void fireblanket$startTrace(final @NotNull TraceLevel level, final int depth);

	void fireblanket$stopTrace(final int depth);
}
