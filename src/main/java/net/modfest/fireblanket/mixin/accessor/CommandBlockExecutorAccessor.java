package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.world.level.BaseCommandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(BaseCommandBlock.class)
public interface CommandBlockExecutorAccessor {
	@Accessor
	long getLastExecution();
}
