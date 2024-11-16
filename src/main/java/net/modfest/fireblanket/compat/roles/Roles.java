package net.modfest.fireblanket.compat.roles;

import net.minecraft.entity.player.PlayerEntity;

public class Roles {
	private static boolean isSingleplayer(PlayerEntity player) {
		return player.getServer() != null && player.getServer().isSingleplayer();
	}

	public static boolean isNetadmin(PlayerEntity player) {
		if (player == null) {
			return false;
		}

		if (PlayerRolesCompat.isLoaded) {
			if (PlayerRolesCompat.isNetadmin(player)) {
				return true;
			}
		}

		if (isSingleplayer(player)) {
			return true;
		}

		// check fb config as well
		return false;
	}

	public static boolean isOrganizer(PlayerEntity player) {
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

		if (isSingleplayer(player)) {
			return true;
		}

		// check fb config as well
		return false;
	}

	public static boolean isBuilder(PlayerEntity player) {
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

		if (isSingleplayer(player)) {
			return true;
		}

		// check fb config as well
		return false;
	}
}
