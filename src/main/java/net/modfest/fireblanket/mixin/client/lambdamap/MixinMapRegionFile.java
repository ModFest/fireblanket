package net.modfest.fireblanket.mixin.client.lambdamap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets="dev/lambdaurora/lambdamap/map/storage/MapRegionFile")
public class MixinMapRegionFile {
	
	@Inject(at=@At("HEAD"), method="saveChunk", cancellable=true)
	public void saveChunk(@Coerce Object mapChunk, CallbackInfo ci) {
		// Does DEFLATE on-thread. Laggy as fuck and we don't need it since we're shipping a
		// complete map with the pack.
		ci.cancel();
	}

}
