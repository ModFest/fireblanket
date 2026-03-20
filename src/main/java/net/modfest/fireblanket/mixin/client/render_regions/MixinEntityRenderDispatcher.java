package net.modfest.fireblanket.mixin.client.render_regions;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.client.ClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

	@ModifyReturnValue(at = @At("RETURN"), method = "shouldRender")
	private static boolean shouldRender(boolean original, Entity entity, Frustum frustum, double x, double y, double z) {
		return original && (!ClientState.useRegionRenderer || FireblanketClient.shouldRender(entity));
	}
}
