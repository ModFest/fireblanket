package net.modfest.fireblanket.mixin.client.bakery.sign;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.mixinsupport.client.RetrofitBakery;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Ampflower
 * @since ${version}
 **/
@Mixin(AbstractSignRenderer.class)
public class MixinAbstractSignRenderer<E extends SignBlockEntity, S extends SignRenderState> implements RetrofitBakery<E, S> {

	/**
	 * @vanilla-copy {@link AbstractSignRenderer#extractRenderState(SignBlockEntity, SignRenderState, float, Vec3, ModelFeatureRenderer.CrumblingOverlay)}
	 */
	@Override
	public void extractBakingRenderState(final E blockEntity, final S state, final int light) {
		RetrofitBakery.super.extractBakingRenderState(blockEntity, state, light);
		state.maxTextLineWidth = blockEntity.getMaxTextLineWidth();
		state.textLineHeight = blockEntity.getTextLineHeight();
		state.frontText = blockEntity.getText(SignTextSlot.FRONT);
		state.backText = blockEntity.getText(SignTextSlot.BACK);
		state.isTextFilteringEnabled = Minecraft.getInstance().isTextFilteringEnabled();
		// PARITY CHANGE: Bake as if we're always in range or scoping.
		//  We're baking it down, we cannot use any distance check.
		state.drawOutline = true;
	}

	/**
	 * @author Ampflower
	 * @reason intentionally break other mixins into here, minimize stuff copied to the render state
	 */
	@Override
	@Overwrite
	@MustBeInvokedByOverriders
	public void extractRenderState(
		final E blockEntity,
		final S state,
		final float partialTicks,
		final Vec3 cameraPosition,
		final ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		RetrofitBakery.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
	}

	// TODO: Some kind of copy then rename dynamic mixin?
	/**
	 * @author Ampflower
	 * @reason Intentionally break other mixins into here.
	 */
	@Override
	@Overwrite
	public void submit(
		final S state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector,
		final CameraRenderState camera
	) {
		// no-op
	}

	/**
	 * @vanilla-copy {@link AbstractSignRenderer#submit(SignRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)}
	 */
	@Override
	public void submitForBaking(
		final S state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector
	) {
		if (state.frontText != null) {
			poseStack.pushPose();
			poseStack.mulPose(state.transformations.frontText());
			this.submitSignText(state, poseStack, submitNodeCollector, state.frontText);
			poseStack.popPose();
		}

		if (state.backText != null) {
			poseStack.pushPose();
			poseStack.mulPose(state.transformations.backText());
			this.submitSignText(state, poseStack, submitNodeCollector, state.backText);
			poseStack.popPose();
		}
	}

	@Shadow
	private void submitSignText(
		final S state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector,
		final SignText text
	) {
		throw new AssertionError();
	}
}
