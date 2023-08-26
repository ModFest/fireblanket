package net.modfest.fireblanket.mixin.sounds;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SpyglassItem;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpyglassItem.class)
public class MixinSpyglassItem {
	@Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
	private void fireblanket$dontPlaySoundStart(PlayerEntity instance, SoundEvent sound, float volume, float pitch) {

	}

	@Redirect(method = "playStopUsingSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"))
	private void fireblanket$dontPlaySoundStop(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch) {

	}
}
