package net.modfest.fireblanket.client.render;

import net.minecraft.client.render.VertexFormatElement;

public interface ExtendedVertexFormat {
    Element[] fireblanket$getExtendedElements();

    static record Element(VertexFormatElement actual, int increment, int byteLength) { }
}
