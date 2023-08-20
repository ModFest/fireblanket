package net.modfest.fireblanket.net;

import java.io.IOException;
import java.util.List;
import com.github.luben.zstd.ZstdInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.modfest.fireblanket.EndlessByteBufInputStream;

public class ZstdDecoder extends MessageToMessageDecoder<ByteBuf> {

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
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		stream.close();
	}
	
}
