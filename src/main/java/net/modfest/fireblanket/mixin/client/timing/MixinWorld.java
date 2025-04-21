package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(World.class)
public abstract class MixinWorld {
	@Shadow
	@Nullable
	public abstract BlockEntity getBlockEntity(BlockPos pos);

	@Shadow
	public abstract boolean isClient();

	@Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
	private void fireblanket$measureBETick(BlockEntityTickInvoker instance) {
		if (this.isClient() && ClientState.displayTickTimes && this.getBlockEntity(instance.getPos()) instanceof ObservableTicks observe) {
			long start = System.nanoTime();
			instance.tick();
			observe.fireblanket$setTickTime(System.nanoTime() - start);
		} else {
			instance.tick();
		}
	}
}
