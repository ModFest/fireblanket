package net.modfest.fireblanket.mixin.client.bakery.shelf;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ShelfRenderer;
import net.minecraft.client.renderer.blockentity.state.ShelfRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.modfest.fireblanket.mixinsupport.client.RetrofitBakery;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * @author Ampflower
 **/
@NullMarked
@Mixin(ShelfRenderer.class)
public abstract class MixinShelfRenderer implements RetrofitBakery<ShelfBlockEntity, ShelfRenderState> {
	@Shadow
	@Final
	private ItemModelResolver itemModelResolver;

	/**
	 * @author Ampflower
	 * @reason No-op: Intentionally break any mixins into here.
	 */
	@Override
	@Overwrite
	public void extractRenderState(
		final ShelfBlockEntity blockEntity,
		final ShelfRenderState state,
		final float partialTicks,
		final Vec3 cameraPosition,
		final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
	) {
	}

	/**
	 * @vanilla-copy {@link ShelfRenderer#extractRenderState(ShelfBlockEntity, ShelfRenderState, float, Vec3, ModelFeatureRenderer.CrumblingOverlay)}
	 */
	@Override
	public void extractBakingRenderState(
		final ShelfBlockEntity blockEntity,
		final ShelfRenderState state,
		final int light
	) {
		RetrofitBakery.super.extractBakingRenderState(blockEntity, state, light);
		state.alignToBottom = blockEntity.getAlignItemsToBottom();
		state.facing = blockEntity.getBlockState().getValue(ShelfBlock.FACING);
		NonNullList<ItemStack> items = blockEntity.getItems();
		int seed = HashCommon.long2int(blockEntity.getBlockPos().asLong());

		for (int slot = 0; slot < items.size(); ++slot) {
			ItemStack itemStack = items.get(slot);
			if (!itemStack.isEmpty()) {
				ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
				this.itemModelResolver.updateForTopItem(
					itemStackRenderState,
					itemStack,
					ItemDisplayContext.ON_SHELF,
					blockEntity.level(),
					blockEntity,
					seed + slot
				);
				state.items[slot] = itemStackRenderState;
			}
		}
	}

	/**
	 * @author Ampflower
	 * @reason No-op
	 */
	@Override
	@Overwrite
	public void submit(
		final ShelfRenderState state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector,
		final CameraRenderState camera
	) {
	}

	/**
	 * @vanilla-copy {@link ShelfRenderer#submit(ShelfRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)}
	 */
	@Override
	public void submitForBaking(
		final ShelfRenderState state,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector
	) {

		float yRot = state.facing.getAxis().isHorizontal() ? -state.facing.toYRot() : 180.F;

		for (int slot = 0; slot < state.items.length; slot++) {
			ItemStackRenderState itemState = state.items[slot];
			if (itemState == null) {
				continue;
			}
			submitItem(state, itemState, poseStack, submitNodeCollector, slot, yRot);
		}
	}

	@Override
	public boolean shouldBake(final ShelfBlockEntity shelf) {
		for (final ItemStack stack : shelf.getItems()) {
			if (!stack.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Shadow
	private static void submitItem(
		final ShelfRenderState state,
		final ItemStackRenderState itemStackRenderState,
		final PoseStack poseStack,
		final SubmitNodeCollector submitNodeCollector,
		final int slot,
		final float yRot
	) {
		throw new AssertionError();
	}
}
