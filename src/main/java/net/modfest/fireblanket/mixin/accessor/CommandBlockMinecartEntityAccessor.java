package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(MinecartCommandBlock.class)
public interface CommandBlockMinecartEntityAccessor {
	@Accessor
	int getLastActivated();
}
