package net.modfest.fireblanket.mixin.client.pose_stack;

import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Ampflower
 **/
@Mixin(MatrixStack.Entry.class)
interface AccessorMatrixStackEntry {
	/**
	 * Needed to implement an alternative pop for {@link MixinMatrixStack}.
	 */
	@Invoker
	void invokeCopy(MatrixStack.Entry entry);
}
