package net.modfest.fireblanket.mixin.client.pose_stack;

import net.modfest.fireblanket.stacksmash.Guard;
import net.modfest.fireblanket.stacksmash.GuardProxy;
import net.modfest.fireblanket.stacksmash.StackTracer;
import net.modfest.fireblanket.stacksmash.Tracer;
import net.modfest.fireblanket.stacksmash.TracerProxy;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.NoSuchElementException;

/**
 * @author Ampflower
 **/
@Mixin(value = Matrix4fStack.class, remap = false)
public abstract class MixinMatrix4fStack extends Matrix4f implements TracerProxy, GuardProxy {
	@Unique
	private final StackTracer tracer = new StackTracer();

	@Shadow
	private Matrix4f[] mats;

	@Shadow
	private int curr;

	@Shadow
	public abstract Matrix4fStack clear();

	@Inject(method = "pushMatrix", at = @At("TAIL"))
	private void onPush(CallbackInfoReturnable<Matrix4fStack> ci) {
		this.tracer.fireblanket$push(2);
	}

	@Inject(method = "popMatrix()Lorg/joml/Matrix4fStack;", at = @At("HEAD"), cancellable = true)
	private void onPop(CallbackInfoReturnable<Matrix4fStack> ci) {
		if (this.tracer.fireblanket$pop(2)) {
			return;
		}

		resetHead();

		ci.setReturnValue((Matrix4fStack) (Object) this);
	}

	@Unique
	private void resetHead() {
		if (!this.tracer.fireblanket$isGuardActive()) {
			// Vanilla; Guard blocks the vanilla throw branch.
			// As popping fails only when empty in this state,
			// we can make the assumption that the stack is also empty.
			throw new IllegalStateException("guard: already at the bottom of the stack");
		}

		final int index = this.curr;
		if (index == 0) {
			this.identity();
			return;
		}

		final Matrix4f last = mats[index - 1];
		this.mats[curr].set(last);
		this.set(last);
	}


	@Override
	public Guard fireblanket$wrappedGuard() {
		return this.tracer;
	}

	@Override
	public void fireblanket$guard$popStack(final int delta) {
		if ((this.curr -= delta) < 0) {
			this.clear();
			throw new NoSuchElementException("tried to over-pop: " + delta);
		}
	}

	@Override
	public Tracer fireblanket$wrappedTracer() {
		return this.tracer;
	}
}
