package net.modfest.fireblanket;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.EventLoop;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.util.concurrent.AbstractEventExecutor;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

public class BenchmarkNettyQueue {
	@State(Scope.Benchmark)
	public static class NettyState {
		public EpollEventLoopGroup loopGroup;

		@Setup(Level.Trial)
		public void setup() {
			// Yep. This won't work outside linux. But we're interested in
			// EpollServerSocketChannel's performance so the benchmark isn't valid otherwise
			this.loopGroup = new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
		}

		@TearDown(Level.Trial)
		public void destroy() {
			loopGroup.close();
		}
	}

	@Benchmark
	@Fork(2)
	@Warmup(iterations = 10, time = 50, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 50, time = 10, timeUnit = TimeUnit.MILLISECONDS)
	public void measureExecute(NettyState state) {
		state.loopGroup.execute(() -> {

		});
	}

	@Benchmark
	@Fork(2)
	@Warmup(iterations = 10, time = 50, timeUnit = TimeUnit.MILLISECONDS)
	@Measurement(iterations = 50, time = 10, timeUnit = TimeUnit.MILLISECONDS)
	public void measureLazyExecute(NettyState state) {
		((AbstractEventExecutor)state.loopGroup.next()).lazyExecute(() -> {

		});
	}
}
