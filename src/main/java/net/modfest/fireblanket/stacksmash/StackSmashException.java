package net.modfest.fireblanket.stacksmash;

import java.lang.StackWalker.StackFrame;

/**
 * @author Ampflower
 **/
public class StackSmashException extends RuntimeException {
	public final StackFrame trace;
	public final StackFrame head;

	public StackSmashException(final StackFrame trace, final StackFrame head) {
		this(trace, head, null);
	}

	public StackSmashException(final StackFrame trace, final StackFrame head, final Exception cause) {
		super("Stack smashing detected:\n\ttrace: " + trace + "\n\tcurrent: " + head + "\n---", cause);
		this.trace = trace;
		this.head = head;
	}
}
