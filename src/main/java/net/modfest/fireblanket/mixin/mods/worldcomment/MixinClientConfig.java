package net.modfest.fireblanket.mixin.mods.worldcomment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// fabric prefix because WC uses multi-loader-single-jar garbage
@Pseudo
@Mixin(targets = "fabric.cn.zbx1425.worldcomment.ClientConfig")
public class MixinClientConfig {
	@Shadow
	public boolean commentVisibilityPreference;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	) public void defaultNotVisible(CallbackInfo ci) {
		this.commentVisibilityPreference = false;
	}
}
