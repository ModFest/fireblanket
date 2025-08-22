package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;

import java.util.NoSuchElementException;

/**
 * Common methods for a stack guard that guarantees that stack smashing,
 * over or under popping of the stack, will be caught and reset, or throw an exception.
 * <p>
 * Usecases include loops where a more specific blame would yield more useful results.
 *
 * @author Ampflower
 * @apiNote The implementer is assumed to be joined to a {@link Stack} or equivalent.
 * @see StackTracer
 **/
public interface Guard {

	/**
	 * Pushes a stack guard, allowing detection & force resetting of the stack.
	 * <p>
	 * If the code after tries to pop the stack beyond when the last stack guard was pushed,
	 * the pop function will either...
	 * <ul>
	 *     <li>throw a {@link StackSmashException}</li>
	 *     <li>log and reset the top of the stack</li>
	 *     <li>silently reset the top of the stack</li>
	 * </ul>
	 */
	void fireblanket$pushGuard();

	/**
	 * Checks the stack guard for unpopped matrices, logging and forcefully resetting the stack.
	 *
	 * @return the amount under-popped, if any
	 */
	@CheckReturnValue
	int fireblanket$checkGuard();

	/**
	 * Pops the current stack guard.
	 *
	 * @return the amount under-popped that wasn't checked, if any
	 * @throws NoSuchElementException If there is no stack guard present.
	 * @apiNote Users of this class should call {@link #fireblanket$checkGuard() checkGuard}
	 * 	each iteration if used in a loop.
	 */
	@CheckReturnValue
	int fireblanket$popGuard() throws NoSuchElementException;

	/**
	 * Checks whether the guard would prevent popping if {@link Stack} was popped.
	 *
	 * @return whether the guard blocks pops.
	 * @implSpec If there is no guard, the method shall check the stack against 0.
	 * @apiNote Intended as an internal method of a {@link Stack}; avoid calling manually.
	 */
	@ApiStatus.OverrideOnly
	boolean fireblanket$guardCheck();

	/**
	 * Resets the guard stack.
	 *
	 * @return whether there are still active guards at reset time.
	 */
	@CheckReturnValue
	boolean fireblanket$resetGuard();

	/**
	 * @return whether there are active guards.
	 */
	@CheckReturnValue
	boolean fireblanket$isGuardActive();
}
