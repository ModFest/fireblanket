package net.modfest.fireblanket.mixin.mods.create;

import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = {
	"com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity", //WindmillBearing extends it
	"com.simibubi.create.content.contraptions.bearing.ClockworkBearingBlockEntity"
})
public class MixinMechanicalBearingBlockEntity {
	@Unique private static final int NEW_DEFAULT_LAZY_RATE = 50;
	
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 3), remap = false)
	private int fireblanket$reduceLazyTickRate(int orig) {
		return NEW_DEFAULT_LAZY_RATE;
	}
	
	@Inject(method = "write", at = @At("RETURN"), remap = false)
	public void fireblanket$onWrite(NbtCompound tag, boolean clientPacket, CallbackInfo ci) {
		int i = ((AccessorSmartBlockEntity) this).fireblanket$getLazyTickRate();
		if(i != NEW_DEFAULT_LAZY_RATE) tag.putInt("fbLazy", i);
	}
	
	@Inject(method = "read", at = @At("RETURN"), remap = false)
	public void fireblanket$onRead(NbtCompound tag, boolean clientPacket, CallbackInfo ci) {
		int i = tag.getInt("fbLazy");
		if(i > 0) ((AccessorSmartBlockEntity) this).fireblanket$setLazyTickRate(i);
	}
}
