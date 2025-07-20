package net.modfest.fireblanket.mixin.accessor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientConnection.class)
public interface ClientConnectionAccessor {
	@Invoker("sendImmediately")
	void fireblanket$sendImmediately(Packet<?> packet, @Nullable ChannelFutureListener channelFutureListener, boolean flush);

	@Accessor("channel")
	Channel fireblanket$getChannel();

}
