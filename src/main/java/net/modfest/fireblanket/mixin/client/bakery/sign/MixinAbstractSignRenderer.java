package net.modfest.fireblanket.mixin.client.bakery.sign;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.mixinsupport.client.RetrofitBakery;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
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
		state.frontText = blockEntity.getFrontText();
		state.backText = blockEntity.getBackText();
		state.isTextFilteringEnabled = Minecraft.getInstance().isTextFilteringEnabled();
		// PARITY CHANGE: Bake as if we're always in range or scoping.
		//  We're baking it down, we cannot use any distance check.
		state.drawOutline = true;
		state.woodType = SignBlock.getWoodType(blockEntity.getBlockState().getBlock());
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
		state.woodType = SignBlock.getWoodType(blockEntity.getBlockState().getBlock());
	}

	/**
	 * @author Ampflower
	 * @reason Delegate to crumbling, intentionally break other mixins into here.
	 */
	@Override
	@Overwrite
	public void submit(
		final S state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector,
		final CameraRenderState camera
	) {
		if (state.breakProgress == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.mulPose(state.transformations.body());
		Model.Simple bodyModel = this.getSignModel(state);
		this.submitSign(
			poseStack,
			state.lightCoords,
			state.woodType,
			bodyModel,
			state.breakProgress,
			submitNodeCollector
		);
		poseStack.popPose();
	}

	@Override
	public void submitForBaking(
		final S state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector
	) {
		this.submitSignWithText(state, poseStack, /* Always send */ null, submitNodeCollector);
	}

	@Shadow
	private void submitSignWithText(
		final S state,
		final PoseStack poseStack,
		@Nullable final ModelFeatureRenderer.CrumblingOverlay breakProgress,
		final SubmitNodeCollector submitNodeCollector
	) {
		throw new AssertionError();
	}

	@Shadow
	protected void submitSign(
		final PoseStack poseStack,
		final int lightCoords,
		final WoodType type,
		final Model.Simple signModel,
		final ModelFeatureRenderer.CrumblingOverlay breakProgress,
		final SubmitNodeCollector submitNodeCollector
	) {
		throw new AssertionError();
	}

	@Shadow
	protected Model.Simple getSignModel(S state) {
		throw new AssertionError();
	}

	@Shadow
	protected SpriteId getSignSprite(WoodType type) {
		throw new AssertionError();
	}

	@Shadow
	private static boolean isOutlineVisible(final BlockPos pos) {
		throw new AssertionError();
	}
}
