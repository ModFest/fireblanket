package net.modfest.fireblanket.mixin.opto;

import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Debug(export = true)
@Mixin(MapState.class)
public class MixinMapState {
	@Shadow @Final private boolean showDecorations;

	public MixinMapState() {}

	{
		this.showDecorations = false;
	}
}
