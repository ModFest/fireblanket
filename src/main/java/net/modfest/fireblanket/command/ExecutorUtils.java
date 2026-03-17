package net.modfest.fireblanket.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.modfest.fireblanket.mixin.accessor.CommandBlockMinecartEntityAccessor;
import net.modfest.fireblanket.mixinsupport.CommandBE;
import net.modfest.fireblanket.util.TextUtil;

/**
 * @author Ampflower
 * @implNote Package private as the majority of the utilities in here are irrelevant for most.
 * @see DumpCommand
 * @see CmdFindReplaceCommand
 */
final class ExecutorUtils {

	private static final Style
		IMPULSE_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD),
		CHAIN_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA),
		REPEATING_STYLE = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE),
		MINECART_STYLE = Style.EMPTY, // intentionally blank
		UNKNOWN_STYLE = Style.EMPTY; // intentionally blank

	private static final Style
		UNCONDITIONAL_STYLE = Style.EMPTY.withColor(ChatFormatting.RED),
		CONDITIONAL_STYLE = Style.EMPTY.withColor(ChatFormatting.GRAY);

	private static final Style
		NEEDS_REDSTONE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY),
		ALWAYS_ACTIVE_STYLE = Style.EMPTY.withColor(ChatFormatting.YELLOW),
		POWERED_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_RED);


	// Mojang had fun with their translation strings :3
	private static final Component
		IMPULSE = Component.translatable("advMode.mode.redstone").setStyle(IMPULSE_STYLE),
		CHAIN = Component.translatable("advMode.mode.sequence").setStyle(CHAIN_STYLE),
		REPEATING = Component.translatable("advMode.mode.auto").setStyle(REPEATING_STYLE),
		MINECART = Component.translatable("entity.minecraft.minecart").setStyle(MINECART_STYLE),
		UNKNOWN = Component.translatableWithFallback("fireblanket.unknown", "???").setStyle(UNKNOWN_STYLE);


	// Graphical shorthands
	private static final Component IMPULSE_EMOJI = Component.literal("🔳")
		.setStyle(IMPULSE_STYLE.withHoverEvent(new HoverEvent.ShowText(IMPULSE)));

	private static final Component CHAIN_EMOJI = Component.literal("⛓")
		.setStyle(CHAIN_STYLE.withHoverEvent(new HoverEvent.ShowText(CHAIN)));

	private static final Component REPEATING_EMOJI = Component.literal("🔁")
		.setStyle(REPEATING_STYLE.withHoverEvent(new HoverEvent.ShowText(REPEATING)));

	// I heard you went shopping for commands.
	private static final Component MINECART_EMOJI = Component.literal("🛒")
		.setStyle(MINECART_STYLE.withHoverEvent(new HoverEvent.ShowText(MINECART)));

	// I got a gift for you. I don't know what's inside, sorry.
	// I can't even tell you if I wanted to, I was just told to give it to you.
	// "\uD83C\uDF81"
	private static final Component UNKNOWN_EMOJI = Component.literal("📦")
		.setStyle(UNKNOWN_STYLE.withHoverEvent(new HoverEvent.ShowText(UNKNOWN)));


	// Powered state; note: Always Active takes precedence over Powered
	private static final Component
		NEEDS_REDSTONE = Component.translatable("advMode.mode.redstoneTriggered").setStyle(NEEDS_REDSTONE_STYLE),
		ALWAYS_ACTIVE = Component.translatable("advMode.mode.autoexec.bat").setStyle(ALWAYS_ACTIVE_STYLE),
		POWERED = Component.translatableWithFallback("fireblanket.advMode.powered", "Powered").setStyle(POWERED_STYLE);


	private static final Component NEEDS_REDSTONE_EMOJI = Component.literal("🔌")
		.setStyle(NEEDS_REDSTONE_STYLE.withHoverEvent(new HoverEvent.ShowText(NEEDS_REDSTONE)));
	private static final Component ALWAYS_ACTIVE_EMOJI = Component.literal("💡")
		.setStyle(ALWAYS_ACTIVE_STYLE.withHoverEvent(new HoverEvent.ShowText(ALWAYS_ACTIVE)));
	private static final Component POWERED_EMOJI = Component.literal("🔦")
		.setStyle(POWERED_STYLE.withHoverEvent(new HoverEvent.ShowText(POWERED)));


	// Conditional state
	private static final Component
		UNCONDITIONAL = Component.translatable("advMode.mode.unconditional").setStyle(UNCONDITIONAL_STYLE),
		CONDITIONAL = Component.translatable("advMode.mode.conditional").setStyle(CONDITIONAL_STYLE);

	private static final Component UNCONDITIONAL_EMOJI = Component.literal("❗")
		.setStyle(UNCONDITIONAL_STYLE.withHoverEvent(new HoverEvent.ShowText(UNCONDITIONAL)));
	private static final Component CONDITIONAL_EMOJI = Component.literal("❓")
		.setStyle(CONDITIONAL_STYLE.withHoverEvent(new HoverEvent.ShowText(CONDITIONAL)));

	/**
	 * Converts the given command block to emoji speak.
	 * <p>
	 * Each emoji denotes the type and state of the command block in question.
	 */
	// Note: 1.21.9/1.22 will allow using the item sprite in text.
	// Should we migrate to that once we port?
	static MutableComponent toEmojiSpeak(final BlockEntity be) {
		final BlockState state = be.getBlockState();

		final MutableComponent type = Component.empty();
		if (state.is(Blocks.COMMAND_BLOCK)) {
			type.append(IMPULSE_EMOJI);
		} else if (state.is(Blocks.CHAIN_COMMAND_BLOCK)) {
			type.append(CHAIN_EMOJI);
		} else if (state.is(Blocks.REPEATING_COMMAND_BLOCK)) {
			type.append(REPEATING_EMOJI);
		} else {
			type.append(UNKNOWN_EMOJI.copy()
				.withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(state.getBlock().getName()))));
		}

		if (!(be instanceof CommandBlockEntity cbe)) {
			return type;
		}

		if (cbe.isAutomatic()) {
			type.append(ALWAYS_ACTIVE_EMOJI);
		} else if (cbe.isPowered()) {
			type.append(POWERED_EMOJI);
		} else {
			type.append(NEEDS_REDSTONE_EMOJI);
		}

		if (cbe.isConditional()) {
			type.append(CONDITIONAL_EMOJI);
		} else {
			type.append(UNCONDITIONAL_EMOJI);
		}

		return type;
	}

	static Component toBlame(final BlockEntity blockEntity) {
		final MutableComponent location = TextUtil.ofLocationWithTeleport(
			blockEntity.getLevel(),
			blockEntity.getBlockPos()
		).withStyle(ChatFormatting.YELLOW);

		if (blockEntity instanceof CommandBE cbe) {
			location.withStyle(style -> style.withHoverEvent(cbe.fireblanket$getBlame()));
		}

		return ExecutorUtils.toEmojiSpeak(blockEntity)
			.append(" @ ")
			.append(location);
	}

	/**
	 * Converts the given command block to emoji speak.
	 * <p>
	 * Each emoji denotes the type and state of the command block in question.
	 */
	// Note: 1.21.9/1.22 will allow using the item sprite in text.
	// Should we migrate to that once we port?
	static MutableComponent toEmojiSpeak(final Entity e) {
		final MutableComponent type = Component.empty();

		if (!(e instanceof MinecartCommandBlock cbe)) {
			return type.append(UNKNOWN_EMOJI.copy().withStyle(style -> style.withHoverEvent(
				new HoverEvent.ShowEntity(new HoverEvent.EntityTooltipInfo(e.getType(), e.getUUID(), e.getName()))
			)));
		}

		type.append(MINECART_EMOJI);

		// We could've possibly caught it mid-tick, use `<=` instead of `<`.
		if (cbe.tickCount - ((CommandBlockMinecartEntityAccessor) cbe).getLastActivated() <= 4) {
			type.append(POWERED_EMOJI);
		} else {
			type.append(NEEDS_REDSTONE_EMOJI);
		}

		return type;
	}

	static Component toBlame(final Entity entity) {
		final MutableComponent location = TextUtil.ofLocation(entity.blockPosition())
			.withStyle(style -> style.applyFormat(ChatFormatting.YELLOW)
				.withClickEvent(TextUtil.toClickToTeleport(entity)));

		if (entity instanceof CommandBE cbe) {
			location.withStyle(style -> style.withHoverEvent(cbe.fireblanket$getBlame()));
		}

		return ExecutorUtils.toEmojiSpeak(entity)
			.append(" @ ")
			.append(location);
	}

	// Admittedly this could be better (i.e. off the shelf tooling?)
	// But not really sure how you'd do that.
	static MutableComponent buildDuration(long ticks) {
		if (ticks == 0) {
			return Component.literal("Now");
		}

		long seconds = ticks / 20;
		ticks %= 20;

		if (seconds == 0) {
			return Component.literal(ticks + " ticks");
		}

		long minutes = seconds / 60;
		seconds %= 60;

		long hours = minutes / 60;
		minutes %= 60;

		long days = hours / 24;
		hours %= 24;

		if (days == 0) {
			return Component.literal(String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, ticks));
		}

		return Component.literal(String.format("%d %02d:%02d:%02d.%02d", days, hours, minutes, seconds, ticks));
	}
}
