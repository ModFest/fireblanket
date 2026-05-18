package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.network.Varint21FrameDecoder;
import net.modfest.fireblanket.mixinsupport.modifiers.Conflict;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Conflict("krypton") // Also lifts the limit.
@Mixin(Varint21FrameDecoder.class)
public class MixinSplitterHandler {

	@ModifyConstant(constant = @Constant(intValue = 3), method = {"copyVarint", "<init>"})
	private static int fireblanket$liftPacketSizeLimit(int orig) {
		return 5;
	}

}
