package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorStand.class)
public interface ArmorStandEntityAccessor {
	@Accessor
	void setDisabledSlots(int d);
}
