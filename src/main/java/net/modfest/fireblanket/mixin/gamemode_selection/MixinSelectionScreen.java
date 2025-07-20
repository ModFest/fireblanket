package net.modfest.fireblanket.mixin.gamemode_selection;

import net.minecraft.client.gui.screen.GameModeSwitcherScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.modfest.fireblanket.util.CanSwitchGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @see CanSwitchGameMode#canSwitchGameMode(ClientPlayerEntity)
 */
@Mixin(GameModeSwitcherScreen.class)
public class MixinSelectionScreen {
	/**
	 * Replaces a {@link net.minecraft.entity.Entity#hasPermissionLevel(int)} check with the one in {@link CanSwitchGameMode}.
	 */
	@Redirect(method = "apply(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/gui/screen/GameModeSwitcherScreen$GameModeSelection;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z"))
	private static boolean redirectPermissionCheck(ClientPlayerEntity instance, int i) {
		return CanSwitchGameMode.canSwitchGameMode(instance);
	}
}
