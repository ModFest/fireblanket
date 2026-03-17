package net.modfest.fireblanket.util;

import net.minecraft.client.player.LocalPlayer;

public class CanSwitchGameMode {
	/**
	 * By default, minecraft checks the permission level to see if a player can
	 * switch game modes using the quick-switcher. This is a more advanced check
	 * that looks at if the client can use the gamemode command.
	 * <p>
	 * This is useful when using player-roles as that can be used to
	 * give /gamemode without giving the whole permission level
	 * @see net.modfest.fireblanket.mixin.gamemode_selection.MixinSelectionScreen
	 */
	public static boolean canSwitchGameMode(LocalPlayer player) {
		var dispatcher = player.connection.getCommands();
		return dispatcher.getRoot().getChild("gamemode") != null;
	}
}
