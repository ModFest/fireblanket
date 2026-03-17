package net.modfest.fireblanket.mixin.accessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Connection.class)
public interface ClientConnectionAccessor {
	@Invoker("sendPacket")
	void fireblanket$sendImmediately(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush);

	@Accessor("channel")
	Channel fireblanket$getChannel();

}
