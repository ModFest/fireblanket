package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.NotNull;

/**
 * @author Ampflower
 **/
public interface TracerProxy extends Tracer {
	Tracer fireblanket$wrappedTracer();

	@Override
	default void fireblanket$startTrace(final @NotNull TraceLevel level, final int depth) {
		this.fireblanket$wrappedTracer().fireblanket$startTrace(level, depth + 1);
	}

	@Override
	default void fireblanket$stopTrace(final int depth) {
		this.fireblanket$wrappedTracer().fireblanket$stopTrace(depth + 1);
	}
}
