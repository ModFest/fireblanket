package net.modfest.fireblanket.mixin.diagnostics.net;

import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.modfest.fireblanket.diagnostics.ForbiddenStackWalker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Occasionally, you can get unhelpful disconnects.
 * <p>
 * This provides context to the disconnects. Perhaps, too much context.
 *
 * @author Ampflower
 **/
@Mixin({
	ClientboundDisconnectPacket.class,
	ClientboundLoginDisconnectPacket.class
})
public class MixinDisconnect {
	@Inject(method = "<init>", at = @At("HEAD"))
	private static void onConstruction(CallbackInfo ci) {
		ForbiddenStackWalker.dumpStack();
	}
}
