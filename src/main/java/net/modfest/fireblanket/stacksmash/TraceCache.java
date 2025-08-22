package net.modfest.fireblanket.stacksmash;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Ampflower
 **/
// TODO: determine most efficient concurrent design,
//  or to have this be a single-thread design but instanced?
// TODO: consider handing out thread unsafe TraceCaches for efficiency
final class TraceCache {
	// private static final Map<Class<?>, ClassData> cache = new WeakHashMap<>();

	private static final ThreadLocal<Map<Class<?>, ClassData>> cache = ThreadLocal.withInitial(WeakHashMap::new);

	public static TraceElement toElement(final StackWalker.StackFrame frame) {
		return cache.get()
			.computeIfAbsent(frame.getDeclaringClass(), ClassData::new)
			.getFunction(frame);
		/*
		final ClassData data;

		synchronized (cache) {
			data = cache;
		}

		return data.getFunction(frame);
		*/
	}

	private record ClassData(Class<?> parent, Int2ObjectMap<TraceElement> functions) {
		private ClassData(Class<?> parent) {
			this(parent, new Int2ObjectOpenHashMap<>());
		}

		private TraceElement getFunction(final StackWalker.StackFrame frame) {
			final int index = frame.getByteCodeIndex();

			if (index < 0) {
				return TraceElement.fromFrame(frame);
			}

			//synchronized (functions) {
			return functions.computeIfAbsent(index, (int $) -> TraceElement.fromFrame(frame));
			//}
		}
	}

}
