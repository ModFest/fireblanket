package net.modfest.fireblanket.net;

import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import com.github.luben.zstd.ZstdOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.util.ReassignableOutputStream;

public class ZstdEncoder extends MessageToByteEncoder<ByteBuf> {

	private static final ScheduledExecutorService sched = Executors.newSingleThreadScheduledExecutor();
	
	private static final LongAdder inBytes = new LongAdder();
	private static final LongAdder outBytes = new LongAdder();
	
	static {
		sched.scheduleAtFixedRate(() -> {
			Fireblanket.LOGGER.info("Zstd ratio past 5m: "+((double)inBytes.sumThenReset()/outBytes.sumThenReset()));
		}, 5, 5, TimeUnit.MINUTES);
	}
	
	private final long flushFrequency, unclogFrequency;
	
	private final ReassignableOutputStream out;
	private final ZstdOutputStream stream;

	private ScheduledFuture<?> future = null;
	
	private long lastFlush = System.nanoTime();

	public ZstdEncoder(ReassignableOutputStream out, ZstdOutputStream stream, long flushFrequency) {
		this.out = out;
		this.stream = stream;
		this.flushFrequency = flushFrequency;
		this.unclogFrequency = (flushFrequency*3)/2;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
		if (future != null) {
			future.cancel(false);
			future = null;
		}
		inBytes.add(msg.readableBytes());
		this.out.setDelegate(new ByteBufOutputStream(out));
		int start = out.writerIndex();
		new ByteBufInputStream(msg, false).transferTo(stream);
		if (flushFrequency == 0 || System.nanoTime()-lastFlush > flushFrequency) {
			lastFlush = System.nanoTime();
			stream.flush();
		} else {
			Channel ch = ctx.channel();
			future = sched.schedule(() -> {
				future = null;
				ch.writeAndFlush(Unpooled.EMPTY_BUFFER);
			}, unclogFrequency, TimeUnit.NANOSECONDS);
		}
		outBytes.add(out.writerIndex()-start);
		this.out.setDelegate(OutputStream.nullOutputStream());
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		stream.close();
	}
	
}
