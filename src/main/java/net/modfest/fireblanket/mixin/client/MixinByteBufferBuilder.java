package net.modfest.fireblanket.mixin.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "Fixes" a memleak by forcefully deallocating on finalization.
 *
 * @author Ampflower
 **/
@Mixin(value = ByteBufferBuilder.class, priority = 0)
public class MixinByteBufferBuilder {
	@Shadow
	public void discard() {
		throw new AssertionError();
	}

	// FIXME: This *should* be using refqueues with a phantom reference holding this buffer as the referent,
	//  and a copy of the pointer. Reallocation needs to be accounted for, which makes this less feasible.
	@Intrinsic
	@Override
	@SuppressWarnings({"deprecated", "removal"}) // see note above
	protected void finalize() throws Throwable {
		// implemented in injection - do not overwrite
		super.finalize();
	}

	@Inject(method = "finalize()V", at = @At("HEAD"))
	private void onFinalize(CallbackInfo ci) {
		this.discard();
	}
}
