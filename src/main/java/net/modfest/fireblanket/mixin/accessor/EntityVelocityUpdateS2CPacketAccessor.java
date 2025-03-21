package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {
	// getters with conflicting names already exist
	@Accessor("velocityX")
	int vx();

	@Accessor("velocityY")
	int vy();

	@Accessor("velocityZ")
	int vz();
}
