package net.modfest.fireblanket.mixinsupport.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.hephaestus.glowcase.client.render.block.entity.BakedBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

/**
 * @author Ampflower
 **/
@NullMarked
public interface RetrofitBakery<E extends BlockEntity, S extends BlockEntityRenderState> extends BakedBlockEntityRenderer<E, S, S> {
	@Override
	default boolean shouldBake(E e) {
		// Well, if you're retrofitting the bakery-
		// You *probably* want this.
		return true;
	}

	@Override
	default S createBakedRenderState() {
		return createRenderState();
	}

	@Override
	@SuppressWarnings({"deprecation", "UnstableApiUsage", "NonExtendableApiUsage"})
	default void submit(
		S state,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState camera
	) {
		throw new AssertionError("Always expected to be extended.");
	}

	/**
	 * @deprecated Extending this is an error.
	 */
	@Override
	@Deprecated(forRemoval = true)
	@ApiStatus.NonExtendable
	default void submitForRendering(
		S s,
		PoseStack poseStack,
		SubmitNodeCollector submitNodeCollector,
		CameraRenderState cameraRenderState
	) {
		throw new AssertionError("Never expected to be called.");
	}

	/**
	 * In the event it isn't: Use {@link org.spongepowered.asm.mixin.Intrinsic}.
	 */
	@Override
	default S createRenderState() {
		throw new AssertionError("Always expected to be extended.");
	}
}
