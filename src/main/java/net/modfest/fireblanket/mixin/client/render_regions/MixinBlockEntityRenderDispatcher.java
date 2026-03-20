package net.modfest.fireblanket.mixin.client.render_regions;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.FireblanketClient;
import net.modfest.fireblanket.client.ClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
	@Inject(at = @At("HEAD"), method = "tryExtractRenderState", cancellable = true)
	private static void render(final CallbackInfoReturnable<?> ci, @Local(argsOnly = true) BlockEntity be) {
		if (ClientState.useRegionRenderer && !FireblanketClient.shouldRender(be)) {
			ci.cancel();
		}
	}

}
