package net.modfest.fireblanket.mixin.client.bufferbuilder_opto;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.modfest.fireblanket.client.render.ExtendedVertexFormat;

@Mixin(VertexFormat.class)
public class MixinVertexFormat implements ExtendedVertexFormat {

    @Shadow @Final
    private ImmutableList<VertexFormatElement> elements;

    private ExtendedVertexFormat.Element[] fireblanket$extendedElements;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fireblanket$createElementArray(ImmutableMap<String, VertexFormatElement> elementMap, CallbackInfo ci) {
        this.fireblanket$extendedElements = new ExtendedVertexFormat.Element[this.elements.size()];

        VertexFormatElement currentElement = elements.get(0);
        int id = 0;
        for (VertexFormatElement element : this.elements) {
            if (element.getType() == VertexFormatElement.Type.PADDING) continue;

            int oldId = id;
            int byteLength = 0;

            do {
                if (++id >= this.fireblanket$extendedElements.length)
                    id -= this.fireblanket$extendedElements.length;
                byteLength += currentElement.getByteLength();
                currentElement = this.elements.get(id);
            } while (currentElement.getType() == VertexFormatElement.Type.PADDING);

            this.fireblanket$extendedElements[oldId] = new ExtendedVertexFormat.Element(element, id - oldId, byteLength);
        }
    }

    @Override
    public ExtendedVertexFormat.Element[] fireblanket$getExtendedElements() {
        return this.fireblanket$extendedElements;
    }

}
