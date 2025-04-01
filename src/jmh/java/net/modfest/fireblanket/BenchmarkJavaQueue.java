package net.modfest.fireblanket;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BenchmarkJavaQueue {
	public static Object INSTANCE = new Object();

	@State(Scope.Benchmark)
	public static class LinkedBlockingState {
		public LinkedBlockingQueue<Object> queue;

		@Setup(Level.Trial)
		public void setup() {
			this.queue = new LinkedBlockingQueue<>();
		}

		@TearDown(Level.Trial)
		public void destroy() {
			this.queue = null;
		}
	}

	@State(Scope.Benchmark)
	public static class ConcurrentLinkedState {
		public ConcurrentLinkedQueue<Object> queue;

		@Setup(Level.Trial)
		public void setup() {
			this.queue = new ConcurrentLinkedQueue<>();
		}

		@TearDown(Level.Trial)
		public void destroy() {
			this.queue = null;
		}
	}

	@Benchmark
	@Warmup(iterations = 10, time = 50, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 50, time = 10, timeUnit = TimeUnit.MILLISECONDS)
	public void benchmarkLinkedBlockingQueue(LinkedBlockingState state) {
		state.queue.add(INSTANCE);
	}

	@Benchmark
	@Warmup(iterations = 10, time = 50, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 50, time = 10, timeUnit = TimeUnit.MILLISECONDS)
	public void benchmarkConcurrentLinkedQueue(ConcurrentLinkedState state) {
		state.queue.add(INSTANCE);
	}
}
