package net.modfest.fireblanket.net;

import com.github.luben.zstd.ZstdInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.modfest.fireblanket.util.EndlessByteBufInputStream;

import java.io.IOException;
import java.util.List;

public class ZstdDecoder extends ByteToMessageDecoder {

	private final ByteBuf inBuf = Unpooled.buffer();

	private final ZstdInputStream stream;

	public ZstdDecoder() throws IOException {
		this.stream = new ZstdInputStream(new EndlessByteBufInputStream(inBuf));
		this.stream.setContinuous(true);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		inBuf.writeBytes(msg);
		ByteBuf buf = ctx.alloc().buffer();
		stream.transferTo(new ByteBufOutputStream(buf));
		out.add(buf);
		inBuf.discardSomeReadBytes();
	}

	@Override
	public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
		// super.handlerRemoved(ctx);
		stream.close();
	}

}
