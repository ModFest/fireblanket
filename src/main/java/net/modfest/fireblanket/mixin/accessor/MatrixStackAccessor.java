package net.modfest.fireblanket.mixin.accessor;

import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Deque;

@Mixin(MatrixStack.class)
public interface MatrixStackAccessor {
	@Accessor
	Deque<MatrixStack.Entry> getStack();
}
