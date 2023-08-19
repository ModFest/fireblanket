package net.modfest.fireblanket.mixin.client.render_regions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.modfest.fireblanket.FireblanketClient;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

	@Inject(at=@At("RETURN"), method="shouldRender", cancellable=true)
	public void shouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> ci) {
		if (ci.getReturnValueZ() && !FireblanketClient.shouldRender(entity)) {
			ci.setReturnValue(false);
		}
	}
	
}
