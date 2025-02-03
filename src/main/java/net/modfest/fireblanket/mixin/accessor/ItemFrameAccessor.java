package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.entity.decoration.ItemFrameEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemFrameEntity.class)
public interface ItemFrameAccessor {
	@Accessor
	void setFixed(boolean v);
}
