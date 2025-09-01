package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(CommandBlockMinecartEntity.class)
public interface CommandBlockMinecartEntityAccessor {
	@Accessor
	int getLastExecuted();
}
