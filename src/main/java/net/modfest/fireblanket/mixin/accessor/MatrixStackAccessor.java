package net.modfest.fireblanket.mixin.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoseStack.class)
public interface MatrixStackAccessor {
//	@Accessor
//	Deque<MatrixStack.Entry> getStack();
}
