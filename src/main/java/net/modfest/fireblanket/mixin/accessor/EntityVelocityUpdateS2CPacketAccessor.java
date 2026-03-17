package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientboundSetEntityMotionPacket.class)
public interface EntityVelocityUpdateS2CPacketAccessor {
	// getters with conflicting names already exist
	@Accessor("xa")
	int vx();

	@Accessor("ya")
	int vy();

	@Accessor("za")
	int vz();
}
