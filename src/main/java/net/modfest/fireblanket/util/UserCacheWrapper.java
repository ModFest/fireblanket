package net.modfest.fireblanket.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Ampflower
 **/
public final class UserCacheWrapper {
	private static final Set<UUID> invalid = new LinkedHashSet<>();


	public static @Nullable String getProfileName(
		final MinecraftServer server,
		final UUID uuid
	) {
		if (invalid.contains(uuid)) {
			return null;
		}

		final ServerPlayer player = server.getPlayerList().getPlayer(uuid);

		if (player != null) {
			return player.getGameProfile().getName();
		}

		final GameProfileCache cache = server.getProfileCache();

		final Optional<GameProfile> profile = cache.get(uuid);

		if (profile.isPresent()) {
			return profile.get().getName();
		}

		final ProfileResult result = server.getSessionService().fetchProfile(uuid, true);

		if (result == null) {
			invalid.add(uuid);
			return null;
		}

		cache.add(result.profile());

		return result.profile().getName();
	}
}
