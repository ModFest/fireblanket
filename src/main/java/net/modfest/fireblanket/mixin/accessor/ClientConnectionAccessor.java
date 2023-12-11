package net.modfest.fireblanket.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;

@Mixin(ClientConnection.class)
public interface ClientConnectionAccessor {

	@Invoker("sendImmediately")
	void fireblanket$sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush);

	@Accessor("channel")
	Channel fireblanket$getChannel();
	
}
