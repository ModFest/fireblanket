package net.modfest.fireblanket.util;

import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * @author Ampflower
 */
public final class TextUtil {
	public static Text getEntityName(final Entity entity) {
		Text name = entity.getDisplayName();
		if (name != null) {
			return name;
		}
		return entity.getName();
	}

	public static Text ofEntityWithTeleport(final Entity entity) {
		final ClickEvent clickEvent = new ClickEvent.SuggestCommand("/tp " + entity.getUuidAsString());

		return getEntityName(entity).copy().styled(style -> style.withClickEvent(clickEvent));
	}

	public static Text ofLocationWithTeleport(final World world, final Vec3i pos) {
		final MutableText text = Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());

		if (world == null) {
			return text;
		}

		final ClickEvent clickEvent = new ClickEvent.SuggestCommand(
			"/execute in " + world.getRegistryKey().getValue() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
		);

		return text.setStyle(Style.EMPTY.withClickEvent(clickEvent));
	}

	public static Text getPlayerName(final MinecraftServer server, final @Nullable UUID uuid, String name) {
		if (uuid == null) {
			return Text.of(name);
		}

		final ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);

		if (player != null) {
			// This probably can lie, but this is likely the most helpful.
			return player.getDisplayName();
		}

		final ProfileResult result = server.getSessionService().fetchProfile(uuid, true);

		if (result != null) {
			name = result.profile().getName();
		}

		final HoverEvent.EntityContent content = new HoverEvent.EntityContent(EntityType.PLAYER, uuid, Text.of(name));
		final HoverEvent event = new HoverEvent.ShowEntity(content);

		return Text.literal(name).setStyle(Style.EMPTY.withHoverEvent(event));
	}
}
