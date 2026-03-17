package net.modfest.fireblanket.mixin.block;

import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(ServerboundSetCommandMinecartPacket.class)
interface AccessorUpdateCommandBlockMinecartC2SPacket {
	@Accessor
	int getEntity();
}
