package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientLevel.class)
public class MixinClientWorld {
	@Redirect(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
	private void fireblanket$measureTick(Entity instance) {
		if (ClientState.displayingEntityTickTimes) {
			ObservableTicks observe = (ObservableTicks) instance;
			long start = System.nanoTime();
			instance.tick();
			observe.fireblanket$setTickTime(System.nanoTime() - start);
		} else {
			instance.tick();
		}
	}

	@Redirect(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
	private void fireblanket$measureRideTick(Entity instance) {
		if (ClientState.displayingEntityTickTimes) {
			ObservableTicks observe = (ObservableTicks) instance;
			long start = System.nanoTime();
			instance.rideTick();
			observe.fireblanket$setTickTime(System.nanoTime() - start);
		} else {
			instance.rideTick();
		}
	}
}
