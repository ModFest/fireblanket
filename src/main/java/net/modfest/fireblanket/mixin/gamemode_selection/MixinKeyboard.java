package net.modfest.fireblanket.mixin.gamemode_selection;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionSet;
import net.modfest.fireblanket.util.CanSwitchGameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see CanSwitchGameMode#canSwitchGameMode(LocalPlayer)
 */
@Mixin(KeyboardHandler.class)
public class MixinKeyboard {
	@Shadow
	@Final
	private Minecraft minecraft;

	/**
	 * Replaces the {@link PermissionCheck#check(PermissionSet)} check for
	 * using {@code f3+n} and {@code f3+f4} with the one in {@link CanSwitchGameMode}.
	 */
	@Definition(id = "PERMISSION_CHECK", field = "Lnet/minecraft/server/commands/GameModeCommand;PERMISSION_CHECK:Lnet/minecraft/server/permissions/PermissionCheck;")
	@Definition(id = "check", method = "Lnet/minecraft/server/permissions/PermissionCheck;check(Lnet/minecraft/server/permissions/PermissionSet;)Z")
	@Expression("PERMISSION_CHECK.check(?)")
	@Redirect(method = "handleDebugKeys", at = @At("MIXINEXTRAS:EXPRESSION"))
	private boolean redirectPermissionCheck(PermissionCheck ignoredCheck, PermissionSet ignoredSet) {
		return CanSwitchGameMode.canSwitchGameMode(this.minecraft.player);
	}
}
