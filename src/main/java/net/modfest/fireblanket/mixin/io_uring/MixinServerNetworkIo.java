package net.modfest.fireblanket.mixin.io_uring;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.incubator.channel.uring.IOUring;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.util.Lazy;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.mixinsupport.IOUringSupport;

@Mixin(ServerNetworkIo.class)
public class MixinServerNetworkIo {

	@ModifyConstant(constant=@Constant(stringValue="Using epoll channel type"), method="bind")
	public String fireblanket$fixLogMessage(String orig) {
		if (IOUringSupport.ENABLED) {
			if (IOUring.isAvailable()) {
				return "Using io_uring channel type";
			} else {
				Fireblanket.LOGGER.error("Could not enable io_uring", IOUring.unavailabilityCause());
				return "Using epoll channel type (io_uring requested, but not available)";
			}
		}
		return orig;
	}
	
	@ModifyVariable(at=@At(value="INVOKE", target="org/slf4j/Logger.info(Ljava/lang/String;)V", ordinal=0), method="bind", ordinal=0)
	public Class<? extends ServerSocketChannel> fireblanket$useIoUringClass(Class<? extends ServerSocketChannel> orig) {
		if (IOUringSupport.ENABLED && IOUring.isAvailable()) {
			return IOUringServerSocketChannel.class;
		}
		return orig;
	}
	
	@ModifyVariable(at=@At(value="INVOKE", target="org/slf4j/Logger.info(Ljava/lang/String;)V", ordinal=0), method="bind", ordinal=0)
	public EventLoopGroup fireblanket$useIoUringGroup(EventLoopGroup orig) {
		if (IOUringSupport.ENABLED && IOUring.isAvailable()) {
			return IOUringSupport.CHANNEL.get();
		}
		return orig;
	}
	
}
