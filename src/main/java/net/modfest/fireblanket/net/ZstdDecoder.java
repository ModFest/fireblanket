package net.modfest.fireblanket.net;

import java.util.List;

import com.github.luben.zstd.ZstdInputStream;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.modfest.fireblanket.ReassignableInputStream;

public class ZstdDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final ReassignableInputStream in;
    private final ZstdInputStream stream;

    public ZstdDecoder(ReassignableInputStream in, ZstdInputStream stream) {
        this.in = in;
        this.stream = stream;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().buffer();
        in.setDelegate(new ByteBufInputStream(msg, false));
        stream.transferTo(new ByteBufOutputStream(buf));
        out.add(buf);
    }
    
}
