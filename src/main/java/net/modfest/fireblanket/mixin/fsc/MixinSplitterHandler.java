package net.modfest.fireblanket.mixin.fsc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.network.SplitterHandler;

@Mixin(SplitterHandler.class)
public class MixinSplitterHandler {

	@ModifyConstant(constant=@Constant(intValue=3), method="decode")
	public int fireblanket$liftPacketSizeLimit(int orig) {
		return 5;
	}
	
}
