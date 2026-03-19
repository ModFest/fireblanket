package net.modfest.fireblanket.compat.roles;

import net.minecraft.world.entity.player.Player;
import net.modfest.fireblanket.mixin.accessor.ServerPlayerAccessor;

public final class Roles {
	private static boolean isSingleplayer(Player player) {
		return player instanceof ServerPlayerAccessor serverPlayer && serverPlayer.getServer().isSingleplayer();
	}

	public static boolean isNetadmin(Player player) {
		if (player == null) {
			return false;
		}

		if (PlayerRolesCompat.isLoaded) {
			if (PlayerRolesCompat.isNetadmin(player)) {
				return true;
			}
		}

		return isSingleplayer(player);

		// check fb config as well
	}

	public static boolean isOrganizer(Player player) {
		if (player == null) {
			return false;
		}

		if (isNetadmin(player)) {
			return true;
		}

		if (PlayerRolesCompat.isLoaded) {
			if (PlayerRolesCompat.isOrganizer(player)) {
				return true;
			}
		}

		return isSingleplayer(player);

		// check fb config as well
	}

	public static boolean isBuilder(Player player) {
		if (player == null) {
			return false;
		}

		if (isOrganizer(player)) {
			return true;
		}

		if (PlayerRolesCompat.isLoaded) {
			if (PlayerRolesCompat.isBuilder(player)) {
				return true;
			}
		}

		return isSingleplayer(player);

		// check fb config as well
	}
}
