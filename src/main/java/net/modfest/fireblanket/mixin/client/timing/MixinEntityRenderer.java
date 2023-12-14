package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.client.render.QuadEmitter;
import net.modfest.fireblanket.client.render.RenderLayers;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private <T extends Entity> void fireblanket$renderMaskedEntities(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ClientState.displayTickTimes && entity instanceof ObservableTicks observed) {
            Box box = entity.getBoundingBox();

            long t = observed.fireblanket$getTickTime();
            String s;
            int color = 0xFFFFFF;
            if (t > 1000) {
                t /= 1000;
                s = t + " us";
            } else {
                s = t + " ns";
            }

            DebugRenderer.drawString(matrices, vertexConsumers,  s,
                    box.minX + (box.maxX - box.minX) / 2.0 + 0.5,
                    box.minY + (box.maxY - box.minY) / 2.0 + 0.5,
                    box.minZ + (box.maxZ - box.minZ) / 2.0 + 0.5,
                    color, 0.03F, true, 0, true);
        }
    }
}
