package net.modfest.fireblanket.mixin.gamemode_selection;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.player.LocalPlayer;
import net.modfest.fireblanket.util.CanSwitchGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see CanSwitchGameMode#canSwitchGameMode(LocalPlayer)
 */
@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
	/**
	 * Replaces the {@link net.minecraft.world.entity.Entity#hasPermissionLevel(int)} check for
	 * using {@code f3+n} with the one in {@link CanSwitchGameMode}.
	 */
	@Redirect(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z", ordinal = 1))
	private boolean redirectPermissionCheck(LocalPlayer instance, int i) {
		return CanSwitchGameMode.canSwitchGameMode(instance);
	}

	/**
	 * Replaces the {@link net.minecraft.world.entity.Entity#hasPermissionLevel(int)} check for
	 * using {@code f3+f4} with the one in {@link CanSwitchGameMode}.
	 */
	@Redirect(method = "handleDebugKeys", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z", ordinal = 2))
	private boolean redirectPermissionCheck2(LocalPlayer instance, int i) {
		return CanSwitchGameMode.canSwitchGameMode(instance);
	}
}
