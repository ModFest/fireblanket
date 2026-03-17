package net.modfest.fireblanket.mixin.opto;

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MapItemSavedData.class)
public class MixinMapState {
	@Shadow
	@Final
	private boolean trackingPosition;

	public MixinMapState() {}

	{
		this.trackingPosition = false;
	}
}
