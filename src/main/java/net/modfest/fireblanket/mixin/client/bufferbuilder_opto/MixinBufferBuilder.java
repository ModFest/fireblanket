package net.modfest.fireblanket.mixin.client.bufferbuilder_opto;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder /*extends FixedColorVertexConsumer*/ implements VertexConsumer {

//    @Shadow
//    private ByteBuffer buffer;
//
//    @Shadow
//    private int elementOffset;
//
//    @Shadow
//    private VertexFormatElement currentElement;
//
//    @Shadow
//    private int currentElementId;
//
//    private long fireblanket$pBuffer = -1;
//    private ExtendedVertexFormat.Element[] fireblanket$vertexFormatExtendedElements;
//    private ExtendedVertexFormat.Element fireblanket$currentExtendedElement;
//
//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void fireblanket$getBufferPointer(BufferAllocator allocator, VertexFormat.DrawMode drawMode, VertexFormat format, CallbackInfo ci) {
//        fireblanket$pBuffer = MemoryUtil.memAddress(buffer);
//    }

//    @Inject(method = "grow(I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BufferBuilder;buffer:Ljava/nio/ByteBuffer;", shift = At.Shift.AFTER))
//    private void fireblanket$getGrownBufferPointer(int size, CallbackInfo ci) {
//        fireblanket$pBuffer = MemoryUtil.memAddress(buffer);
//    }
//
//    @Inject(method = "setFormat", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BufferBuilder;format:Lnet/minecraft/client/render/VertexFormat;", shift = At.Shift.AFTER))
//    private void fireblanket$cacheVertexFormatElements(VertexFormat format, CallbackInfo ci) {
//        fireblanket$vertexFormatExtendedElements = ((ExtendedVertexFormat) format).fireblanket$getExtendedElements();
//        fireblanket$currentExtendedElement = fireblanket$vertexFormatExtendedElements[0];
//    }

//    /**
//     * @reason The original `nextElement` is horrible for the JVM. It makes 8 dereferences scattered across the heap, does a modulo when it doesn't have to, and calls it's self
//     * @author Maximum
//     */
//    @Override
//    @Overwrite
//    public void nextElement() {
//        if ((currentElementId += fireblanket$currentExtendedElement.increment()) >= fireblanket$vertexFormatExtendedElements.length)
//            currentElementId -= fireblanket$vertexFormatExtendedElements.length;
//        elementOffset += fireblanket$currentExtendedElement.byteLength();
//        fireblanket$currentExtendedElement = fireblanket$vertexFormatExtendedElements[currentElementId];
//        currentElement = fireblanket$currentExtendedElement.actual();
//
//        if (colorFixed && currentElement.type() == VertexFormatElement.COLOR.type())
//            VertexConsumer.super.color(fixedRed, fixedGreen, fixedBlue, fixedAlpha);
//    }
}
