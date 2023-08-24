package net.modfest.fireblanket.mixin.client.entity_masking;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Box;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.client.render.QuadEmitter;
import net.modfest.fireblanket.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Inject(method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private <T extends Entity> void fireblanket$renderMaskedEntities(T entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (ClientState.MASKED_ENTITIES.contains(entity.getType())) {
            VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayers.TRANSLUCENT_BROKEN_DEPTH);

            Box box = entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ());

            QuadEmitter.buildBox(buffer, matrices, (float) box.minX, (float) box.maxX, (float) box.minY, (float) box.maxY, (float) box.minZ, (float) box.maxZ, 255, 101, 80, 40);

            // Please let this be a normal field trip
            if (vertexConsumers instanceof VertexConsumerProvider.Immediate imm) {
                imm.draw();
            }
        }
    }
}
