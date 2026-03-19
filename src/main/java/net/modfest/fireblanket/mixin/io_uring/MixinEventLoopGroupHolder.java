package net.modfest.fireblanket.mixin.io_uring;

import io.netty.channel.IoHandlerFactory;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;
import io.netty.channel.uring.IoUringSocketChannel;
import net.minecraft.server.network.EventLoopGroupHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Ampflower
 **/
@Mixin(EventLoopGroupHolder.class)
public abstract class MixinEventLoopGroupHolder {
	@Unique
	private static final EventLoopGroupHolder IO_URING = new EventLoopGroupHolder("io_uring", IoUringSocketChannel.class, IoUringServerSocketChannel.class) {
		@Override
		protected IoHandlerFactory ioHandlerFactory() {
			return IoUringIoHandler.newFactory();
		}
	};

	@Inject(
		method = "remote",
		at = @At(value = "INVOKE", target = "Lio/netty/channel/kqueue/KQueue;isAvailable()Z"),
		cancellable = true
	)
	private static void injectIoUring(CallbackInfoReturnable<EventLoopGroupHolder> ci) {
		if (IoUring.isAvailable()) {
			ci.setReturnValue(IO_URING);
		}
	}
}
