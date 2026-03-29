package net.modfest.fireblanket.stacksmash;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.StackWalker.StackFrame;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * @author Ampflower
 **/
public final class StackTracer implements Guard, Stack, Tracer {
	private static final Logger logger = LogUtils.getLogger();

	private final Deque<StackFrame> frames = new ArrayDeque<>();
	private final IntList guards = new IntArrayList();

	private int startingTraceDepth;

	// MatrixStacks are often created on the fly; we need to use a global TraceLevel.
	private TraceLevel level = StackUtil.getTraceLevel();

	@Override
	public void fireblanket$push(int stackDepth) {
		if (this.level == TraceLevel.NONE) {
			// TODO: figure out a better collection?
			this.frames.push(TraceElement.nullFrame);
			return;
		}

		final StackFrame current = StackUtil.getCallerAsProxy(stackDepth);
		this.frames.push(current);
	}

	@Override
	@CheckReturnValue
	public boolean fireblanket$pop(int stackDepth) {
		if (this.fireblanket$guardCheck()) {
			final StackFrame peek = this.frames.peek();
			throw new StackSmashException(peek, StackUtil.getCallerAsProxy(stackDepth));
			//logger.warn("Guard check: attempted to pop {}", peek, );
			//return false;
		}

		final StackFrame trace = this.frames.pop();

		if (this.level != TraceLevel.NONE) {
			final StackFrame current = StackUtil.getCallerAsProxy(stackDepth);

			this.level.logIfMismatched(trace, current);
		}

		return true;
	}

	@Override
	public void fireblanket$startTrace(final @NotNull TraceLevel level, final int depth) {
		if (this.frames.size() != 0) {
			this.frames.poll();
			this.frames.push(StackUtil.getCallerAsProxy(3));
		}
		this.level = level;
		this.startingTraceDepth = this.frames.size();
	}

	@Override
	public void fireblanket$stopTrace(final int depth) {
		final StackFrame frame = StackUtil.getCallerAsProxy(3);
		final StackFrame trace = this.frames.peek();

		this.level.logIfMismatched(trace, frame);

		this.level = TraceLevel.NONE;

		if (this.startingTraceDepth != this.frames.size()) {
			throw new IllegalStateException("Expected depth " + this.startingTraceDepth + ", got " + this.frames.size());
		}
	}

	// StackGuard

	@Override
	public void fireblanket$pushGuard() {
		this.guards.add(this.frames.size());
	}

	@Override
	@CheckReturnValue
	public int fireblanket$checkGuard() {
		return this.forcePop(this.getLastGuard());
	}

	@Override
	@CheckReturnValue
	public int fireblanket$popGuard() throws NoSuchElementException {
		return this.forcePop(this.removeLastGuard());
	}

	@Override
	public boolean fireblanket$guardCheck() {
		return this.frames.size() <= this.getLastGuard();
	}

	/**
	 * Pops all frames since the guard was pushed.
	 */
	private int forcePop(int guard) {
		final int delta = this.frames.size() - guard;
		if (delta <= 0) {
			return 0;
		}
		// TODO: optimise?
		while (this.frames.size() > guard) {
			this.frames.removeLast();
		}
		return delta;
	}

	/**
	 * Returns the last guard if it exists, otherwise returns 0
	 */
	private int getLastGuard() {
		final IntList guards = this.guards;
		// Return 0 if there's no guard.
		if (guards.isEmpty()) {
			return 0;
		}
		return guards.getInt(guards.size() - 1);
	}

	/**
	 * Removes and returns the last guard if it exists, otherwise returns 0
	 */
	private int removeLastGuard() {
		final IntList guards = this.guards;
		// Return 0 if there's no guard.
		if (guards.isEmpty()) {
			return 0;
		}
		return guards.removeInt(guards.size() - 1);
	}

	@Override
	@CheckReturnValue
	public boolean fireblanket$resetGuard() {
		final boolean unpopped = !this.guards.isEmpty();
		this.guards.clear();
		return unpopped;
	}

	@Override
	@CheckReturnValue
	public boolean fireblanket$isGuardActive() {
		return !this.guards.isEmpty();
	}

	public boolean reset() {
		final boolean unpopped = !frames.isEmpty();
		StackFrame frame;

		while ((frame = frames.poll()) != null) {
			logger.warn("Unpopped frame: {}", frame);
		}

		return unpopped;
	}
}
