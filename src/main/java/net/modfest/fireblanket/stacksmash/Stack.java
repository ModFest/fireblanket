package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.CheckReturnValue;

/**
 * @author Ampflower
 * @see StackTracer
 **/
public interface Stack {
	default void fireblanket$push() {
		this.fireblanket$push(0);
	}

	void fireblanket$push(int depth);

	/**
	 * @see #fireblanket$pop(int)
	 */
	@CheckReturnValue
	default boolean fireblanket$pop() {
		return this.fireblanket$pop(0);
	}

	/**
	 * @return whether it has popped.
	 * @implNote If the implementation is not a {@link Guard} or other variant,
	 * 	this function will always return true unless the internal stack is empty.
	 * @apiNote If false, the caller should not pop its own stack,
	 * 	but may behave as if it popped by making the head match the previous entry.
	 * 	Should there be no previous entry, it is up to the implementation to decide.
	 */
	@CheckReturnValue
	boolean fireblanket$pop(int depth);
}
