package net.modfest.fireblanket.util;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author Ampflower
 **/
public final class UserCacheWrapper {
	private static final Set<UUID> invalid = new HashSet<>();

	public static @Nullable String getProfileName(
		final MinecraftServer server,
		final UUID uuid
	) {
		if (invalid.contains(uuid)) {
			return null;
		}

		final ServerPlayer player = server.getPlayerList().getPlayer(uuid);

		if (player != null) {
			return player.getGameProfile().name();
		}

		final UserNameToIdResolver cache = server.services().nameToIdCache();

		final Optional<NameAndId> profile = cache.get(uuid);

		if (profile.isPresent()) {
			return profile.get().name();
		}

		final ProfileResult result = server.services().sessionService().fetchProfile(uuid, true);

		if (result == null) {
			invalid.add(uuid);
			return null;
		}

		cache.add(new NameAndId(result.profile()));

		return result.profile().name();
	}
}
