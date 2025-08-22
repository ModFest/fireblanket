package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.CheckReturnValue;

/**
 * @author Ampflower
 **/
public interface StackProxy extends Stack {
	Stack fireblanket$wrappedStack();

	@Override
	default void fireblanket$push(int depth) {
		this.fireblanket$wrappedStack().fireblanket$push(depth + 1);
	}

	@Override
	@CheckReturnValue
	default boolean fireblanket$pop(int depth) {
		return this.fireblanket$wrappedStack().fireblanket$pop(depth + 1);
	}
}
