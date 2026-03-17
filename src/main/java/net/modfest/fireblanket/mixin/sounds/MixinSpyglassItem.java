package net.modfest.fireblanket.mixin.sounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpyglassItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpyglassItem.class)
public class MixinSpyglassItem {
	@Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
	private void fireblanket$dontPlaySoundStart(Player instance, SoundEvent sound, float volume, float pitch) {

	}

	@Redirect(method = "stopUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
	private void fireblanket$dontPlaySoundStop(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch) {

	}
}
