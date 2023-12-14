package net.modfest.fireblanket.mixin.client.timing;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.mixinsupport.ObservableTicks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
	@Redirect(method = "tickEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
	private void fireblanket$measureTick(Entity instance) {
		if (ClientState.displayTickTimes) {
			ObservableTicks observe = (ObservableTicks) instance;
			long start = System.nanoTime();
			instance.tick();
			observe.fireblanket$setTickTime(System.nanoTime() - start);
		} else {
			instance.tick();
		}
	}
}
