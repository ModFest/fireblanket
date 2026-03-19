package net.modfest.fireblanket.mixin.gamemode_selection;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionSet;
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
	@Redirect(method = "switchToHoveredGameMode(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/debug/GameModeSwitcherScreen$GameModeIcon;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/permissions/PermissionCheck;check(Lnet/minecraft/server/permissions/PermissionSet;)Z"))
	private static boolean redirectPermissionCheck(
		PermissionCheck ignoredCheck,
		PermissionSet ignoredSet,
		@Local(argsOnly = true) Minecraft minecraft
	) {
		return CanSwitchGameMode.canSwitchGameMode(minecraft.player);
	}
}
