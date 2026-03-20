package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * @author Ampflower
 **/
@Mixin(Level.class)
public interface AccessorLevel {
	@Accessor
	List<TickingBlockEntity> getBlockEntityTickers();
}
