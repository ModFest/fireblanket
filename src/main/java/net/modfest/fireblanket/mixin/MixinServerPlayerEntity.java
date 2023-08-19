package net.modfest.fireblanket.mixin;

import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {
	/**
	 * Don't call the tick criteria. On BC23, some mod is serializing the entire player every tick- with how many
	 * mods inject into the serialization, this becomes really expensive really fast.
	 *
	 * @author Jasmine
	 */
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/criterion/TickCriterion;trigger(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
	private void fireblanket$noTickCriteria(TickCriterion instance, ServerPlayerEntity player) {
	}
}
