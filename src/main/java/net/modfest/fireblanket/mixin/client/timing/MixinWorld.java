package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Level.class)
public abstract class MixinWorld {
	@Shadow
	@Nullable
	public abstract BlockEntity getBlockEntity(BlockPos pos);

	@Shadow
	public abstract boolean isClientSide();

	@Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/TickingBlockEntity;tick()V"))
	private void fireblanket$measureBETick(TickingBlockEntity instance) {
		if (this.isClientSide() && ClientState.displayTickTimes && this.getBlockEntity(instance.getPos()) instanceof ObservableTicks observe) {
			long start = System.nanoTime();
			instance.tick();
			observe.fireblanket$setTickTime(System.nanoTime() - start);
		} else {
			instance.tick();
		}
	}
}
