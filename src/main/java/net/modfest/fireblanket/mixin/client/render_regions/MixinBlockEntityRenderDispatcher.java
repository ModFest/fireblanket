package net.modfest.fireblanket.mixin.client.render_regions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.modfest.fireblanket.FireblanketClient;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {

	@Inject(at=@At("HEAD"), method="render(Lnet/minecraft/client/render/block/entity/BlockEntityRenderer;Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", cancellable=true)
	private static void render(BlockEntityRenderer<?> renderer, BlockEntity be, float delta, MatrixStack matrices, VertexConsumerProvider vcp,
			CallbackInfo ci) {
		if (!FireblanketClient.shouldRender(be)) {
			ci.cancel();
		}
	}
	
}
