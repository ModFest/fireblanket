package net.modfest.fireblanket.mixin.fsc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.network.SizePrepender;

@Mixin(SizePrepender.class)
public class MixinSizePrepender {

	@ModifyConstant(constant=@Constant(intValue=3), method="encode")
	public int fireblanket$liftPacketSizeLimit(int orig) {
		return 5;
	}
	
}
