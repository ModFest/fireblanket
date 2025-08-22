package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.NoSuchElementException;

/**
 * @author Ampflower
 **/
public interface GuardProxy extends Guard {
	Guard fireblanket$wrappedGuard();

	@Override
	default void fireblanket$pushGuard() {
		this.fireblanket$wrappedGuard().fireblanket$pushGuard();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec Overriders must call {@link #fireblanket$guard$popStack(int)}.
	 */
	@Override
	@CheckReturnValue
	default int fireblanket$checkGuard() {
		final int delta = this.fireblanket$wrappedGuard().fireblanket$checkGuard();
		this.fireblanket$guard$popStack(delta);
		return delta;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec Overriders must call {@link #fireblanket$guard$popStack(int)}.
	 */
	@Override
	@CheckReturnValue
	default int fireblanket$popGuard() throws NoSuchElementException {
		final int delta = this.fireblanket$wrappedGuard().fireblanket$popGuard();
		this.fireblanket$guard$popStack(delta);
		return delta;
	}

	@Override
	@ApiStatus.OverrideOnly
	default boolean fireblanket$guardCheck() {
		return this.fireblanket$wrappedGuard().fireblanket$guardCheck();
	}

	@Override
	@CheckReturnValue
	default boolean fireblanket$resetGuard() {
		return this.fireblanket$wrappedGuard().fireblanket$resetGuard();
	}

	@Override
	@CheckReturnValue
	default boolean fireblanket$isGuardActive() {
		return this.fireblanket$wrappedGuard().fireblanket$isGuardActive();
	}

	/**
	 * Methods that command popping the stack will call this method.
	 *
	 * @param delta The amount from the stack to force-pop.
	 * @throws NoSuchElementException If the caller tried to over-pop.
	 * @implSpec The implementer must ensure consistency as if the native pop method was called.
	 * 	In the event of an over-pop, {@link NoSuchElementException} must be thrown,
	 * 	and the state left consistent for the event the exception was swallowed.
	 * @apiNote This is not expected to be called directly.
	 * 	If an implementer of this proxy interface overrides
	 *    {@link #fireblanket$checkGuard() checkGuard} and
	 *    {@link #fireblanket$popGuard() popGuard}, it shall call this method.
	 */
	@ApiStatus.OverrideOnly
	void fireblanket$guard$popStack(final int delta);
}
