package net.modfest.fireblanket.mixin.fsc;

import net.minecraft.network.handler.SplitterHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(SplitterHandler.class)
public class MixinSplitterHandler {

	@ModifyConstant(constant=@Constant(intValue=3), method={"shouldSplit", "<init>"})
	private static int fireblanket$liftPacketSizeLimit(int orig) {
		return 5;
	}

}
