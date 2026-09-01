package net.modfest.fireblanket.mixin.serde;

import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerEntity {
	/**
	 * Don't call the tick criteria. On BC23, some mod is serializing the entire player every tick- with how many
	 * mods inject into the serialization, this becomes really expensive really fast.
	 *
	 * @author Jasmine
	 */
	@Redirect(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/triggers/PlayerTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;)V"
		)
	)
	private void fireblanket$noTickCriteria(PlayerTrigger instance, ServerPlayer player) {
	}
}
