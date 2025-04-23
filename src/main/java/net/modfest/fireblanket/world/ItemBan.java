package net.modfest.fireblanket.world;

import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;

import java.util.HashSet;
import java.util.Set;

public class ItemBan {
	public static final Set<String> BANNED_IDS = new HashSet<>();

	public static void apply() {
		BANNED_IDS.addAll(FireblanketConfig.get(ConfigSpecs.BANNED_ITEMS));
	}
}
