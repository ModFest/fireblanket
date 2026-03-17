package net.modfest.fireblanket.mixinsupport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import net.minecraft.util.LazyLoadedValue;

public class IOUringSupport {

	public static final boolean ENABLED = Boolean.getBoolean("fireblanket.useIoUring");

	@SuppressWarnings("deprecation")
	public static final LazyLoadedValue<IOUringEventLoopGroup> CHANNEL = new LazyLoadedValue<>(() ->
		new IOUringEventLoopGroup(0, new ThreadFactoryBuilder()
			.setNameFormat("Netty IO URing Server IO #%d")
			.setDaemon(true)
			.build())
	);


}
