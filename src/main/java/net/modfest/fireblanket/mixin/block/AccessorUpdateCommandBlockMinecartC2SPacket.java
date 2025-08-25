package net.modfest.fireblanket.mixin.block;

import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(UpdateCommandBlockMinecartC2SPacket.class)
interface AccessorUpdateCommandBlockMinecartC2SPacket {
	@Accessor
	int getEntityId();
}
