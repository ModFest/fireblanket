package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.world.CommandBlockExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Ampflower
 **/
@Mixin(CommandBlockExecutor.class)
public interface CommandBlockExecutorAccessor {
	@Accessor
	long getLastExecution();
}
