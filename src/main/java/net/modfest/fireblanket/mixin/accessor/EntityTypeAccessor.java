package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityType.class)
public interface EntityTypeAccessor {
	@Mutable
	@Accessor
	void setMaxTrackDistance(int maxTrackDistance);

	@Mutable
	@Accessor
	void setTrackTickInterval(int trackTickInterval);
}
