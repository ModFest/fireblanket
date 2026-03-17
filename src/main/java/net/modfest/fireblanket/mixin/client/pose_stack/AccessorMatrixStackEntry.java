package net.modfest.fireblanket.mixin.client.pose_stack;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author Ampflower
 **/
@Mixin(PoseStack.Pose.class)
interface AccessorMatrixStackEntry {
	/**
	 * Needed to implement an alternative pop for {@link MixinMatrixStack}.
	 */
	@Invoker
	void invokeSet(PoseStack.Pose entry);
}
