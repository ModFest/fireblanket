package net.modfest.fireblanket.mixin.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "com.simibubi.create.foundation.blockEntity.SmartBlockEntity")
public interface AccessorSmartBlockEntity {
	@Accessor(value = "lazyTickRate", remap = false) int fireblanket$getLazyTickRate();
	@Invoker(value = "setLazyTickRate", remap = false) void fireblanket$setLazyTickRate(int r);
}
