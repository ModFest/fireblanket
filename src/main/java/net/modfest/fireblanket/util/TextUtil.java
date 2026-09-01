package net.modfest.fireblanket.util;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.mixin.accessor.ServerCommandSourceAccessor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Ampflower
 */
public final class TextUtil {
	private static final Logger LOGGER = LogUtils.getLogger();
	// These are not truly immutable (i.e. can be casted to MutableText); guard as such.

	private static final Component warning = Component.literal("⚠").setStyle(
		Style.EMPTY
			.withColor(ChatFormatting.YELLOW)
			.withHoverEvent(new HoverEvent.ShowText(Component.translatable("fireblanket.warning.tooltip")))
	);

	private static final Component server = Component.empty()
		.append(Component.literal("\uD83D\uDDA5️").withStyle(ChatFormatting.GRAY))
		.append(" Server");

	private static final Component spoofed = Component.literal("\uD83C\uDFAD").setStyle(
		Style.EMPTY
			.withColor(ChatFormatting.YELLOW)
			.withHoverEvent(new HoverEvent.ShowText(Component.translatable("fireblanket.spoofed.tooltip")))
	);

	public static final Component unknown = Component.translatableWithFallback("fireblanket.unknown", "???")
		.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);


	public static Component getEntityName(final Entity entity) {
		Component name = entity.getDisplayName();
		if (name != null) {
			return name;
		}
		return entity.getName();
	}

	public static ClickEvent toClickToTeleport(final Entity entity) {
		return new ClickEvent.SuggestCommand("/tp " + entity.getStringUUID());
	}

	public static MutableComponent ofEntityWithTeleport(final Entity entity) {
		final ClickEvent clickEvent = toClickToTeleport(entity);

		return getEntityName(entity).copy().withStyle(style -> style.withClickEvent(clickEvent));
	}

	public static MutableComponent ofLocationWithTeleport(final Level world, final Vec3i pos) {
		final MutableComponent text = ofLocation(pos);

		if (world == null) {
			return text;
		}

		return ofTextWithTeleport(text, world, pos);
	}

	public static MutableComponent ofLocation(final Vec3i pos) {
		return Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
	}

	public static ClickEvent toClickToTeleport(final Level world, final Vec3i pos) {
		return new ClickEvent.SuggestCommand(
			"/execute in " + world.dimension().identifier() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
		);
	}

	public static MutableComponent ofTextWithTeleport(final MutableComponent text, final Level world, final Vec3i pos) {
		return text.withStyle(style -> style.withClickEvent(toClickToTeleport(world, pos)));
	}

	@Deprecated(forRemoval = true)
	public static Component ofCommandBlock(final BaseCommandBlock executor) {
		// FIXME: find a better way of handling this; ReflectionUtils is a bad hack but it's the only usable one.
		try {
			if (executor instanceof MinecartCommandBlock.MinecartCommandBase minecart) {
				return ofEntityWithTeleport(ReflectionUtil.getHost(minecart, MinecartCommandBlock.class))
					.withStyle(style -> style.withHoverEvent(executor.getName().getStyle().getHoverEvent()));
			}

			final CommandBlockEntity entity = ReflectionUtil.getHost(executor, CommandBlockEntity.class);
			return ofTextWithTeleport(
				executor.getName().copy(),
				entity.getLevel(),
				entity.getBlockPos()
			);
		} catch (IllegalAccessException e) {
			LOGGER.warn("Cannot decompose {} for text purposes.", executor, e);
			return Component.literal("Broken: " + executor.getName());
		}
	}

	public static Component ofRunner(final CommandSourceStack source) {
		final Component rawDisplayName;

		if (source.getEntity() instanceof CommandBE be) {
			rawDisplayName = be.fireblanket$getNameWithBlame();
		} else {
			rawDisplayName = Objects.requireNonNullElse(
				((ServerCommandSourceAccessor) source).getNamesProvider().displayName(source.getEntity()),
				unknown
			);
		}

		final Component runner = getRunnerText(source);

		if (source.getEntity() == null || CommandUtils.isRunner(source)) {
			return Objects.requireNonNullElseGet(runner, () -> Component.empty()
				.append(warning)
				.append(" ")
				.append(rawDisplayName)
			);
		}

		final MutableComponent stub = Component.empty()
			// Hardcoding spoofed to avoid translations from hiding the fact.
			.append(spoofed)
			.append(" ");

		if (runner == null) {
			// The runner is unknown, put as-is with quotes.
			return stub
				.append("\"")
				.append(rawDisplayName)
				.append("\"");
		}

		return stub.append(Component.translatableWithFallback(
			"fireblanket.spoofed",
			"%s \"%s\"",
			runner,
			rawDisplayName
		));
	}

	/**
	 * @param server Server to look up the player with.
	 * @param uuid   The UUID to look up.
	 * @param name   The fallback name if the UUID is null.
	 * @return Player display name if it exists, otherwise provided fallback name.
	 * @see #getRawPlayerName(MinecraftServer, UUID, String)
	 */
	public static Component getPlayerName(final MinecraftServer server, final @Nullable UUID uuid, String name) {
		if (uuid == null) {
			return Component.nullToEmpty(name);
		}

		final ServerPlayer player = server.getPlayerList().getPlayer(uuid);

		if (player != null) {
			// This probably can lie, but this is likely the most helpful.
			return player.getDisplayName();
		}

		final String result = UserCacheWrapper.getProfileName(server, uuid);

		if (result != null) {
			name = result;
		}

		final HoverEvent.EntityTooltipInfo content = new HoverEvent.EntityTooltipInfo(
			EntityTypes.PLAYER,
			uuid,
			Component.nullToEmpty(name)
		);
		final HoverEvent event = new HoverEvent.ShowEntity(content);

		return Component.literal(name).setStyle(Style.EMPTY.withHoverEvent(event));
	}

	/**
	 * @param server   Server to look up the player with.
	 * @param uuid     The UUID to look up.
	 * @param fallback The fallback name if the UUID is null.
	 * @return Player name if it exists, else the UUID if provided, otherwise fallback.
	 * @see #getPlayerName(MinecraftServer, UUID, String)
	 */
	public static String getRawPlayerName(
		final MinecraftServer server,
		final @Nullable UUID uuid,
		final String fallback
	) {
		if (uuid == null) {
			return fallback;
		}

		final String result = UserCacheWrapper.getProfileName(server, uuid);

		if (result == null) {
			// better suited to return the UUID than the fallback.
			return uuid.toString();
		}

		return result;
	}

	/**
	 * Returns a UUID without dashes, if with dashes is too long.
	 */
	public static String ofUuidWithNoDashes(final UUID uuid) {
		if (uuid == null) {
			return "null";
		}

		return "%016x%016x".formatted(
			uuid.getLeastSignificantBits(),
			uuid.getMostSignificantBits()
		);
	}

	public static HoverEvent toBlameHover(
		final CommandBE executor,
		final ServerLevel level,
		final BlockPos position,
		final Component name
	) {
		final Component creator = Component.literal(TextUtil.getRawPlayerName(
			level.getServer(),
			executor.fireblanket$getOwner(),
			"Unknown"
		)).withStyle(ChatFormatting.YELLOW);

		final Component creatorUuid = Component.literal(TextUtil.ofUuidWithNoDashes(executor.fireblanket$getOwner()))
			.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		final Component updater = Component.literal(TextUtil.getRawPlayerName(
			level.getServer(),
			executor.fireblanket$getLastUpdate(),
			"Unknown"
		)).withStyle(ChatFormatting.YELLOW);

		final Component updaterUuid = Component.literal(TextUtil.ofUuidWithNoDashes(executor.fireblanket$getLastUpdate()))
			.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		final Component location = TextUtil.ofLocation(position)
			.withStyle(ChatFormatting.YELLOW);

		final Component world = Component.literal(level.dimension().identifier().toString())
			.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		final Component hover = Component.translatableWithFallback(
			"fireblanket.command.blame",
			"%s\nC: %s - %s\nU: %s - %s\n@: %s @ %s",
			name,
			creator,
			creatorUuid,
			updater,
			updaterUuid,
			location,
			world
		);

		return new HoverEvent.ShowText(hover);
	}

	public static Component getBlockEntityName(final BlockEntity entity) {
		return entity.getBlockState().getBlock().getName();
	}

	private static Component getRunnerText(final CommandSourceStack source) {
		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		if (output == null || output == CommandSource.NULL) {
			return null;
		}

		if (output == source.getServer()) {
			return server;
		}

		// FIXME: this really needs to be rethought: individual sources should be implementing this rather than this.

		final ServerPlayer player = CommandUtils.getPlayerRunner(source);

		if (player != null) {
			return ofEntityWithTeleport(player);
		}

		try {
			// FIXME: This is horrible and needs to be replaced ASAP
			final Object host = ReflectionUtil.getHost(output);
			if (host instanceof BaseCommandBlock exec) {
				return ofCommandBlock(exec);
			}
		} catch (IllegalAccessException | IllegalArgumentException e) {
			LOGGER.warn("Invalid: {}", output, e);
		}

		final Class<?> clazz = output.getClass();

		final String name = clazz.getSimpleName();

		// Require a strict name check
		return Component.literal(name.isBlank() ? clazz.toString() : name)
			.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(clazz.getName()))));
	}
}
