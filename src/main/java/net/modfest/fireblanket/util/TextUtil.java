package net.modfest.fireblanket.util;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.modfest.fireblanket.command.CommandUtils;
import net.modfest.fireblanket.mixin.accessor.ServerCommandSourceAccessor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Ampflower
 */
public final class TextUtil {
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
			"/execute in " + world.dimension().location() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
		);
	}

	public static MutableComponent ofTextWithTeleport(final MutableComponent text, final Level world, final Vec3i pos) {
		return text.withStyle(style -> style.withClickEvent(toClickToTeleport(world, pos)));
	}

	public static Component ofCommandBlock(final BaseCommandBlock executor) {
		if (executor instanceof MinecartCommandBlock.MinecartCommandBase minecart) {
			return ofEntityWithTeleport(minecart.getMinecart())
				.withStyle(style -> style.withHoverEvent(executor.getName().getStyle().getHoverEvent()));
		}

		return ofTextWithTeleport(executor.getName().copy(), executor.getLevel(), BlockPos.containing(executor.getPosition()));
	}

	public static Component ofRunner(final CommandSourceStack source) {
		final Component rawDisplayName = Objects.requireNonNullElse(
			((ServerCommandSourceAccessor) source).fireblanket$getRawDisplayName(),
			unknown
		);

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

		final HoverEvent.EntityTooltipInfo content = new HoverEvent.EntityTooltipInfo(EntityType.PLAYER, uuid, Component.nullToEmpty(name));
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

	public static HoverEvent toBlameHover(final BaseCommandBlock executor) {
		final CommandBE cbe = (CommandBE) executor;

		final Component name = getCommandBlockName(executor);

		final Component creator = Component.literal(TextUtil.getRawPlayerName(
			executor.getLevel().getServer(),
			cbe.fireblanket$getOwner(),
			"Unknown"
		)).withStyle(ChatFormatting.YELLOW);

		final Component creatorUuid = Component.literal(TextUtil.ofUuidWithNoDashes(cbe.fireblanket$getOwner()))
			.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		final Component updater = Component.literal(TextUtil.getRawPlayerName(
			executor.getLevel().getServer(),
			cbe.fireblanket$getLastUpdate(),
			"Unknown"
		)).withStyle(ChatFormatting.YELLOW);

		final Component updaterUuid = Component.literal(TextUtil.ofUuidWithNoDashes(cbe.fireblanket$getLastUpdate()))
			.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

		final Component location = TextUtil.ofLocation(BlockPos.containing(executor.getPosition()))
			.withStyle(ChatFormatting.YELLOW);

		final Component world = Component.literal(executor.getLevel().dimension().location().toString())
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

	private static Component getCommandBlockName(final BaseCommandBlock executor) {
		if (executor instanceof MinecartCommandBlock.MinecartCommandBase minecart) {
			return minecart.getMinecart().getType().getDescription();
		}

		try {
			final BlockEntity blockEntity = ReflectionUtil.getHost(executor, BlockEntity.class);

			if (blockEntity != null) {
				return blockEntity.getBlockState().getBlock().getName();
			} else {
				return Component.nullToEmpty(executor.getClass().getName());
			}
		} catch (IllegalAccessException e) {
			return Component.nullToEmpty(e.getMessage());
		}
	}

	private static Component getRunnerText(final CommandSourceStack source) {
		final CommandSource output = ((ServerCommandSourceAccessor) source).getSource();

		if (output == null || output == CommandSource.NULL) {
			return null;
		}

		if (output == source.getServer()) {
			return server;
		}

		if (output instanceof BaseCommandBlock executor) {
			return ofCommandBlock(executor);
		}

		final ServerPlayer player = CommandUtils.getPlayerRunner(source);

		if (player != null) {
			return ofEntityWithTeleport(player);
		}

		final Class<?> clazz = output.getClass();

		final String name = clazz.getSimpleName();

		// Require a strict name check
		return Component.literal(name.isBlank() ? clazz.toString() : name)
			.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty(clazz.getName()))));
	}
}
