package net.modfest.fireblanket.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
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

	private static final Text warning = Text.literal("⚠").setStyle(
		Style.EMPTY
			.withColor(Formatting.YELLOW)
			.withHoverEvent(new HoverEvent.ShowText(Text.translatable("fireblanket.warning.tooltip")))
	);

	private static final Text server = Text.empty()
		.append(Text.literal("\uD83D\uDDA5️").formatted(Formatting.GRAY))
		.append(" Server");

	private static final Text spoofed = Text.literal("\uD83C\uDFAD").setStyle(
		Style.EMPTY
			.withColor(Formatting.YELLOW)
			.withHoverEvent(new HoverEvent.ShowText(Text.translatable("fireblanket.spoofed.tooltip")))
	);

	private static final Text unknown = Text.translatableWithFallback("fireblanket.unknown", "???")
		.formatted(Formatting.GRAY, Formatting.ITALIC);


	public static Text getEntityName(final Entity entity) {
		Text name = entity.getDisplayName();
		if (name != null) {
			return name;
		}
		return entity.getName();
	}

	public static MutableText ofEntityWithTeleport(final Entity entity) {
		final ClickEvent clickEvent = new ClickEvent.SuggestCommand("/tp " + entity.getUuidAsString());

		return getEntityName(entity).copy().styled(style -> style.withClickEvent(clickEvent));
	}

	public static Text ofLocationWithTeleport(final World world, final Vec3i pos) {
		final MutableText text = ofLocation(pos);

		if (world == null) {
			return text;
		}

		return ofTextWithTeleport(text, world, pos);
	}

	public static MutableText ofLocation(final Vec3i pos) {
		return Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
	}

	public static ClickEvent toClickToTeleport(final World world, final Vec3i pos) {
		return new ClickEvent.SuggestCommand(
			"/execute in " + world.getRegistryKey().getValue() + " run tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
		);
	}

	public static Text ofTextWithTeleport(final MutableText text, final World world, final Vec3i pos) {
		return text.styled(style -> style.withClickEvent(toClickToTeleport(world, pos)));
	}

	public static Text ofCommandBlock(final CommandBlockExecutor executor) {
		if (executor instanceof CommandBlockMinecartEntity.CommandExecutor minecart) {
			return ofEntityWithTeleport(minecart.getMinecart())
				.styled(style -> style.withHoverEvent(executor.getName().getStyle().getHoverEvent()));
		}

		return ofTextWithTeleport(executor.getName().copy(), executor.getWorld(), BlockPos.ofFloored(executor.getPos()));
	}

	public static Text ofRunner(final ServerCommandSource source) {
		final Text rawDisplayName = Objects.requireNonNullElse(
			((ServerCommandSourceAccessor) source).fireblanket$getRawDisplayName(),
			unknown
		);

		final Text runner = getRunnerText(source);

		if (source.getEntity() == null || CommandUtils.isRunner(source)) {
			return Objects.requireNonNullElseGet(runner, () -> Text.empty()
				.append(warning)
				.append(" ")
				.append(rawDisplayName)
			);
		}

		final MutableText stub = Text.empty()
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

		return stub.append(Text.translatableWithFallback(
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
	public static Text getPlayerName(final MinecraftServer server, final @Nullable UUID uuid, String name) {
		if (uuid == null) {
			return Text.of(name);
		}

		final ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);

		if (player != null) {
			// This probably can lie, but this is likely the most helpful.
			return player.getDisplayName();
		}

		final String result = UserCacheWrapper.getProfileName(server, uuid);

		if (result != null) {
			name = result;
		}

		final HoverEvent.EntityContent content = new HoverEvent.EntityContent(EntityType.PLAYER, uuid, Text.of(name));
		final HoverEvent event = new HoverEvent.ShowEntity(content);

		return Text.literal(name).setStyle(Style.EMPTY.withHoverEvent(event));
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

	public static HoverEvent toBlameHover(final CommandBlockExecutor executor) {
		final CommandBE cbe = (CommandBE) executor;

		final Text name = getCommandBlockName(executor);

		final Text creator = Text.literal(TextUtil.getRawPlayerName(
			executor.getWorld().getServer(),
			cbe.fireblanket$getOwner(),
			"Unknown"
		)).formatted(Formatting.YELLOW);

		final Text creatorUuid = Text.literal(TextUtil.ofUuidWithNoDashes(cbe.fireblanket$getOwner()))
			.formatted(Formatting.GRAY, Formatting.ITALIC);

		final Text updater = Text.literal(TextUtil.getRawPlayerName(
			executor.getWorld().getServer(),
			cbe.fireblanket$getLastUpdate(),
			"Unknown"
		)).formatted(Formatting.YELLOW);

		final Text updaterUuid = Text.literal(TextUtil.ofUuidWithNoDashes(cbe.fireblanket$getLastUpdate()))
			.formatted(Formatting.GRAY, Formatting.ITALIC);

		final Text location = TextUtil.ofLocation(BlockPos.ofFloored(executor.getPos()))
			.formatted(Formatting.YELLOW);

		final Text world = Text.literal(executor.getWorld().getRegistryKey().getValue().toString())
			.formatted(Formatting.GRAY, Formatting.ITALIC);

		final Text hover = Text.translatableWithFallback(
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

	private static Text getCommandBlockName(final CommandBlockExecutor executor) {
		if (executor instanceof CommandBlockMinecartEntity.CommandExecutor minecart) {
			return minecart.getMinecart().getType().getName();
		}

		try {
			final BlockEntity blockEntity = ReflectionUtil.getHost(executor, BlockEntity.class);

			if (blockEntity != null) {
				return blockEntity.getCachedState().getBlock().getName();
			} else {
				return Text.of(executor.getClass().getName());
			}
		} catch (IllegalAccessException e) {
			return Text.of(e.getMessage());
		}
	}

	private static Text getRunnerText(final ServerCommandSource source) {
		final CommandOutput output = ((ServerCommandSourceAccessor) source).getOutput();

		if (output == null || output == CommandOutput.DUMMY) {
			return null;
		}

		if (output == source.getServer()) {
			return server;
		}

		if (output instanceof CommandBlockExecutor executor) {
			return ofCommandBlock(executor);
		}

		final ServerPlayerEntity player = CommandUtils.getPlayerRunner(source);

		if (player != null) {
			return ofEntityWithTeleport(player);
		}

		final Class<?> clazz = output.getClass();

		final String name = clazz.getSimpleName();

		// Require a strict name check
		return Text.literal(name.isBlank() ? clazz.toString() : name)
			.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(Text.of(clazz.getName()))));
	}
}
