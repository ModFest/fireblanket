package net.modfest.fireblanket.mixin.client.bufferbuilder_opto;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.modfest.fireblanket.client.render.ExtendedVertexFormat;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder extends FixedColorVertexConsumer implements BufferVertexConsumer {

    @Shadow
    private ByteBuffer buffer;

    @Shadow
    private int elementOffset;

    @Shadow
    private VertexFormatElement currentElement;

    @Shadow
    private int currentElementId;

    private long fireblanket$pBuffer = -1;
    private ExtendedVertexFormat.Element[] fireblanket$vertexFormatExtendedElements;
    private ExtendedVertexFormat.Element fireblanket$currentExtendedElement;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fireblanket$getBufferPointer(int initialCapacity, CallbackInfo ci) {
        fireblanket$pBuffer = MemoryUtil.memAddress(buffer);
    }

    @Inject(method = "grow(I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BufferBuilder;buffer:Ljava/nio/ByteBuffer;", shift = At.Shift.AFTER))
    private void fireblanket$getGrownBufferPointer(int size, CallbackInfo ci) {
        fireblanket$pBuffer = MemoryUtil.memAddress(buffer);
    }

    @Inject(method = "setFormat", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BufferBuilder;format:Lnet/minecraft/client/render/VertexFormat;", shift = At.Shift.AFTER))
    private void fireblanket$cacheVertexFormatElements(VertexFormat format, CallbackInfo ci) {
        fireblanket$vertexFormatExtendedElements = ((ExtendedVertexFormat) format).fireblanket$getExtendedElements();
        fireblanket$currentExtendedElement = fireblanket$vertexFormatExtendedElements[0];
    }

    /**
     * @reason The original `nextElement` is horrible for the JVM. It makes 8 dereferences scattered across the heap, does a modulo when it doesn't have to, and calls it's self
     * @author Maximum
     */
    @Override
    @Overwrite
    public void nextElement() {
        if ((currentElementId += fireblanket$currentExtendedElement.increment()) >= fireblanket$vertexFormatExtendedElements.length)
            currentElementId -= fireblanket$vertexFormatExtendedElements.length;
        elementOffset += fireblanket$currentExtendedElement.byteLength();
        fireblanket$currentExtendedElement = fireblanket$vertexFormatExtendedElements[currentElementId];
        currentElement = fireblanket$currentExtendedElement.actual();

        if (colorFixed && currentElement.getType() == VertexFormatElement.Type.COLOR)
            BufferVertexConsumer.super.color(fixedRed, fixedGreen, fixedBlue, fixedAlpha);
    }

    /**
     * @reason Minor performance improvement
     * @author Maximum
     */
    @Override
    @Overwrite
    public void putByte(int index, byte value) {
        MemoryUtil.memPutByte(fireblanket$pBuffer + elementOffset + index, value);
    }

    /**
     * @reason Minor performance improvement
     * @author Maximum
     */
    @Override
    @Overwrite
    public void putShort(int index, short value) {
        MemoryUtil.memPutShort(fireblanket$pBuffer + elementOffset + index, value);
    }

    /**
     * @reason Minor performance improvement
     * @author Maximum
     */
    @Override
    @Overwrite
    public void putFloat(int index, float value) {
        MemoryUtil.memPutFloat(fireblanket$pBuffer + elementOffset + index, value);
    }

}
