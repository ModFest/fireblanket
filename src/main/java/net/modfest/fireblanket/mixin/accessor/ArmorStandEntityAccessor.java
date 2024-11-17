package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorStandEntity.class)
public interface ArmorStandEntityAccessor {
	@Accessor
	void setDisabledSlots(int d);
}
