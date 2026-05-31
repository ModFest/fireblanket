package net.modfest.fireblanket.util;

/**
 * @author Ampflower
 **/
public final class Throwables {
	public static AssertionError assertSuppressed(String message, Throwable cause, Throwable... suppressed) {
		final AssertionError error = new AssertionError(message, cause);
		for (final Throwable value : suppressed) {
			error.addSuppressed(value);
		}

		return error;
	}

	public interface AllocationTraceable {
		Throwable fireblanket$allocationTrace();
	}
}
