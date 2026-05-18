package net.modfest.fireblanket.mixin.client.bakery.sign;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.WallAndGroundTransformations;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
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
@Mixin(HangingSignRenderer.class)
public class MixinHangingSignRenderer extends MixinAbstractSignRenderer<SignBlockEntity, HangingSignRenderState> {
	@Shadow
	@Final
	public static WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS;

	/**
	 * @vanilla-copy {@link HangingSignRenderer#extractRenderState(SignBlockEntity, HangingSignRenderState, float, Vec3, ModelFeatureRenderer.CrumblingOverlay)} )}
	 */
	@Override
	public void extractBakingRenderState(
		final SignBlockEntity blockEntity,
		final HangingSignRenderState state,
		final int light
	) {
		super.extractBakingRenderState(blockEntity, state, light);
		BlockState blockState = blockEntity.getBlockState();
		state.attachmentType = HangingSignBlock.getAttachmentPoint(blockState);
		if (blockState.getBlock() instanceof WallHangingSignBlock) {
			state.transformations = TRANSFORMATIONS.wallTransformation(blockState.getValue(WallHangingSignBlock.FACING));
		} else {
			state.transformations = TRANSFORMATIONS.freeTransformations(blockState.getValue(CeilingHangingSignBlock.ROTATION));
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
