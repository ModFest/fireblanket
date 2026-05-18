package net.modfest.fireblanket.mixin.client.bakery.sign;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.WallAndGroundTransformations;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Ampflower
 **/
@Mixin(StandingSignRenderer.class)
public class MixinStandingSignRenderer extends MixinAbstractSignRenderer<SignBlockEntity, StandingSignRenderState> {
	@Shadow
	@Final
	public static WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS;

	/**
	 * @vanilla-copy {@link StandingSignRenderer#extractRenderState(SignBlockEntity, StandingSignRenderState, float, Vec3, ModelFeatureRenderer.CrumblingOverlay)}
	 */
	@Override
	public void extractBakingRenderState(
		final SignBlockEntity blockEntity,
		final StandingSignRenderState state,
		final int light
	) {
		super.extractBakingRenderState(blockEntity, state, light);
		BlockState blockState = blockEntity.getBlockState();
		state.attachmentType = PlainSignBlock.getAttachmentPoint(blockState);
		if (blockState.getBlock() instanceof WallSignBlock) {
			state.transformations = TRANSFORMATIONS.wallTransformation(blockState.getValue(WallSignBlock.FACING));
		} else {
			state.transformations = TRANSFORMATIONS.freeTransformations(blockState.getValue(StandingSignBlock.ROTATION));
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
	private void onExtractRenderState(
		final CallbackInfo ci,
		final @Local(argsOnly = true) @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		if (breakProgress == null) {
			ci.cancel();
		}
	}
}
