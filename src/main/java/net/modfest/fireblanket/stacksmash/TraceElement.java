package net.modfest.fireblanket.stacksmash;

import org.jetbrains.annotations.Nullable;

/**
 * Alternate light-weight {@link java.lang.StackWalker.StackFrame} implementation that caches relevant data.
 * <p>
 * At current, this is just an immutable StackFrame,
 * but it could be later extended to add metadata,
 * like whether it was a mixin or not.
 *
 * @author Ampflower
 **/
record TraceElement(
	Class<?> parent,
	String fileName,
	String methodName,
	// TODO: Figure out whether to do MethodType instead?
	//  This takes a higher performance hit in comparison if we use the original frame function.
	//  Tho, caveat to be had: needs a interner.
	String descriptor,
	int bytecodeIndex,
	int lineNumber
) implements StackWalker.StackFrame {

	public static final TraceElement nullFrame = new TraceElement(
		Void.class,
		null,
		"!!! invalid !!!",
		"()V",
		-1,
		-1
	);

	public static TraceElement fromFrame(final StackWalker.StackFrame frame) {
		// It'd be silly to reconvert the same information.
		// Just return the input if this is the case.
		if (frame instanceof TraceElement traceElement) {
			return traceElement;
		}

		final StackTraceElement trace = frame.toStackTraceElement();

		final Class<?> parent = frame.getDeclaringClass();
		final String methodName = frame.getMethodName().intern();
		final String descriptor = frame.getDescriptor().intern();
		final int bytecodeIndex = frame.getByteCodeIndex();

		// intern'd later as nullable
		@Nullable String fileName = trace.getFileName();
		final int lineNumber = trace.getLineNumber();

		if (fileName != null) {
			fileName = fileName.intern();
		}

		return new TraceElement(parent, fileName, methodName, descriptor, bytecodeIndex, lineNumber);
	}

	@Override
	public String getClassName() {
		return this.parent().getName();
	}

	@Override
	public String getMethodName() {
		return this.methodName();
	}

	@Override
	public String getDescriptor() {
		return this.descriptor();
	}

	// TODO: determine if getMethodType is worthwhile; we do have sufficient data to reconstruct one

	@Override
	public Class<?> getDeclaringClass() {
		return this.parent();
	}

	@Override
	public int getByteCodeIndex() {
		return this.bytecodeIndex();
	}

	@Override
	public String getFileName() {
		return this.fileName();
	}

	@Override
	public int getLineNumber() {
		return this.lineNumber();
	}

	@Override
	public boolean isNativeMethod() {
		return this.bytecodeIndex() < 0;
	}

	@Override
	public StackTraceElement toStackTraceElement() {
		final Module module = this.parent().getModule();
		return new StackTraceElement(
			this.parent().getClassLoader().getName(),
			module.getName(),
			module.getDescriptor().rawVersion().orElse(null),
			this.parent().getName(),
			this.methodName(),
			this.fileName(),
			this.lineNumber()
		);
	}

	/**
	 * @return whether {@code other} is from the same method.
	 */
	public boolean isSameFunction(final TraceElement other) {
		if (this == other) {
			return true;
		}
		return this.parent() == other.parent()
			&& this.methodName().equals(other.methodName())
			&& this.descriptor().equals(other.descriptor());
	}

	@Override
	public String toString() {
		return String.format("%s%s%s @ %s:%d", parent.descriptorString(), methodName, descriptor, fileName, lineNumber);
	}
}
