package net.modfest.fireblanket.mixin.client.pose_stack;

import com.mojang.blaze3d.vertex.PoseStack;
import net.modfest.fireblanket.stacksmash.Guard;
import net.modfest.fireblanket.stacksmash.GuardProxy;
import net.modfest.fireblanket.stacksmash.StackTracer;
import net.modfest.fireblanket.stacksmash.Tracer;
import net.modfest.fireblanket.stacksmash.TracerProxy;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Ampflower
 **/
@Mixin(PoseStack.class)
public class MixinMatrixStack implements TracerProxy, GuardProxy {
	@Unique
	private final StackTracer tracer = new StackTracer();

	@Shadow
	@Final
	private List<PoseStack.Pose> poses;

	@Shadow
	private int lastIndex;

	@Shadow
	public void setIdentity() {
		throw new AssertionError();
	}

	@Inject(method = "pushPose", at = @At("TAIL"))
	private void onPush(CallbackInfo ci) {
		this.tracer.fireblanket$push(2);
	}

	@Inject(method = "popPose", at = @At("HEAD"), cancellable = true)
	private void onPop(CallbackInfo ci) {
		if (this.tracer.fireblanket$pop(2)) {
			return;
		}

		resetHead();

		ci.cancel();
	}

	@Unique
	private void resetHead() {
		if (!this.tracer.fireblanket$isGuardActive()) {
			// Vanilla; Guard blocks the vanilla throw branch.
			// As popping fails only when empty in this state,
			// we can make the assumption that the stack is also empty.
			throw new NoSuchElementException();
		}

		final int index = this.lastIndex;
		if (index == 0) {
			this.setIdentity();
			return;
		}

		final AccessorMatrixStackEntry head = (AccessorMatrixStackEntry) (Object) this.poses.get(index);

		head.invokeSet(this.poses.get(index - 1));
	}

	@Override
	public final Tracer fireblanket$wrappedTracer() {
		return this.tracer;
	}

	@Override
	public final Guard fireblanket$wrappedGuard() {
		return this.tracer;
	}

	@Override
	public void fireblanket$guard$popStack(final int delta) {
		if ((this.lastIndex -= delta) < 0) {
			// Defensive set in case the stack gets reused despite the illegal state.
			this.lastIndex = 0;
			this.setIdentity();
			throw new NoSuchElementException("tried to over-pop: " + delta);
		}
	}
}
