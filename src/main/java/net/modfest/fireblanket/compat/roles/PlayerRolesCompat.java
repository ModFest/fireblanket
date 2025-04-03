package net.modfest.fireblanket.compat.roles;

import dev.gegy.roles.api.PlayerRolesApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

public class PlayerRolesCompat {
	public static boolean isLoaded = false;

	public static void init() {
		if (FabricLoader.getInstance().isModLoaded("player_roles")) {
			isLoaded = true;
		}
	}

	public static boolean isNetadmin(PlayerEntity player) {
		return is(player, "netadmin");
	}

	public static boolean isOrganizer(PlayerEntity player) {
		return is(player, "organizer") || is(player, "team");
	}

	public static boolean isBuilder(PlayerEntity player) {
		return is(player, "builder") || is(player, "fixer") || is(player, "participant");
	}

	private static boolean is(PlayerEntity player, String id) {
		return PlayerRolesApi.lookup().byPlayer(player).stream().anyMatch(r -> r.getId().equals(id));
	}
}
