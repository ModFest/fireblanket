package net.modfest.fireblanket.mixin.gamemode_selection;

import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.player.LocalPlayer;
import net.modfest.fireblanket.util.CanSwitchGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see CanSwitchGameMode#canSwitchGameMode(LocalPlayer)
 */
@Mixin(GameModeSwitcherScreen.class)
public class MixinSelectionScreen {
	/**
	 * Replaces a {@link net.minecraft.world.entity.Entity#hasPermissionLevel(int)} check with the one in {@link CanSwitchGameMode}.
	 */
	@Redirect(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasPermissions(I)Z"))
	private static boolean redirectPermissionCheck(LocalPlayer instance, int i) {
		return CanSwitchGameMode.canSwitchGameMode(instance);
	}
}
