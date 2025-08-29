package net.modfest.fireblanket.command;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
		IMPULSE_STYLE = Style.EMPTY.withColor(Formatting.GOLD),
		CHAIN_STYLE = Style.EMPTY.withColor(Formatting.AQUA),
		REPEATING_STYLE = Style.EMPTY.withColor(Formatting.LIGHT_PURPLE),
		MINECART_STYLE = Style.EMPTY, // intentionally blank
		UNKNOWN_STYLE = Style.EMPTY; // intentionally blank

	private static final Style
		UNCONDITIONAL_STYLE = Style.EMPTY.withColor(Formatting.RED),
		CONDITIONAL_STYLE = Style.EMPTY.withColor(Formatting.GRAY);

	private static final Style
		NEEDS_REDSTONE_STYLE = Style.EMPTY.withColor(Formatting.DARK_GRAY),
		ALWAYS_ACTIVE_STYLE = Style.EMPTY.withColor(Formatting.YELLOW),
		POWERED_STYLE = Style.EMPTY.withColor(Formatting.DARK_RED);


	// Mojang had fun with their translation strings :3
	private static final Text
		IMPULSE = Text.translatable("advMode.mode.redstone").setStyle(IMPULSE_STYLE),
		CHAIN = Text.translatable("advMode.mode.sequence").setStyle(CHAIN_STYLE),
		REPEATING = Text.translatable("advMode.mode.auto").setStyle(REPEATING_STYLE),
		MINECART = Text.translatable("entity.minecraft.minecart").setStyle(MINECART_STYLE),
		UNKNOWN = Text.translatableWithFallback("fireblanket.unknown", "???").setStyle(UNKNOWN_STYLE);


	// Graphical shorthands
	private static final Text IMPULSE_EMOJI = Text.literal("🔳")
		.setStyle(IMPULSE_STYLE.withHoverEvent(new HoverEvent.ShowText(IMPULSE)));

	private static final Text CHAIN_EMOJI = Text.literal("⛓")
		.setStyle(CHAIN_STYLE.withHoverEvent(new HoverEvent.ShowText(CHAIN)));

	private static final Text REPEATING_EMOJI = Text.literal("🔁")
		.setStyle(REPEATING_STYLE.withHoverEvent(new HoverEvent.ShowText(REPEATING)));

	// I heard you went shopping for commands.
	private static final Text MINECART_EMOJI = Text.literal("🛒")
		.setStyle(MINECART_STYLE.withHoverEvent(new HoverEvent.ShowText(MINECART)));

	// I got a gift for you. I don't know what's inside, sorry.
	// I can't even tell you if I wanted to, I was just told to give it to you.
	// "\uD83C\uDF81"
	private static final Text UNKNOWN_EMOJI = Text.literal("📦")
		.setStyle(UNKNOWN_STYLE.withHoverEvent(new HoverEvent.ShowText(UNKNOWN)));


	// Powered state; note: Always Active takes precedence over Powered
	private static final Text
		NEEDS_REDSTONE = Text.translatable("advMode.mode.redstoneTriggered").setStyle(NEEDS_REDSTONE_STYLE),
		ALWAYS_ACTIVE = Text.translatable("advMode.mode.autoexec.bat").setStyle(ALWAYS_ACTIVE_STYLE),
		POWERED = Text.translatableWithFallback("fireblanket.advMode.powered", "Powered").setStyle(POWERED_STYLE);


	private static final Text NEEDS_REDSTONE_EMOJI = Text.literal("🔌")
		.setStyle(NEEDS_REDSTONE_STYLE.withHoverEvent(new HoverEvent.ShowText(NEEDS_REDSTONE)));
	private static final Text ALWAYS_ACTIVE_EMOJI = Text.literal("💡")
		.setStyle(ALWAYS_ACTIVE_STYLE.withHoverEvent(new HoverEvent.ShowText(ALWAYS_ACTIVE)));
	private static final Text POWERED_EMOJI = Text.literal("🔦")
		.setStyle(POWERED_STYLE.withHoverEvent(new HoverEvent.ShowText(POWERED)));


	// Conditional state
	private static final Text
		UNCONDITIONAL = Text.translatable("advMode.mode.unconditional").setStyle(UNCONDITIONAL_STYLE),
		CONDITIONAL = Text.translatable("advMode.mode.conditional").setStyle(CONDITIONAL_STYLE);

	private static final Text UNCONDITIONAL_EMOJI = Text.literal("❗")
		.setStyle(UNCONDITIONAL_STYLE.withHoverEvent(new HoverEvent.ShowText(UNCONDITIONAL)));
	private static final Text CONDITIONAL_EMOJI = Text.literal("❓")
		.setStyle(CONDITIONAL_STYLE.withHoverEvent(new HoverEvent.ShowText(CONDITIONAL)));

	/**
	 * Converts the given command block to emoji speak.
	 * <p>
	 * Each emoji denotes the type and state of the command block in question.
	 */
	// Note: 1.21.9/1.22 will allow using the item sprite in text.
	// Should we migrate to that once we port?
	static MutableText toEmojiSpeak(final BlockEntity be) {
		final BlockState state = be.getCachedState();

		final MutableText type = Text.empty();
		if (state.isOf(Blocks.COMMAND_BLOCK)) {
			type.append(IMPULSE_EMOJI);
		} else if (state.isOf(Blocks.CHAIN_COMMAND_BLOCK)) {
			type.append(CHAIN_EMOJI);
		} else if (state.isOf(Blocks.REPEATING_COMMAND_BLOCK)) {
			type.append(REPEATING_EMOJI);
		} else {
			type.append(UNKNOWN_EMOJI.copy()
				.styled(style -> style.withHoverEvent(new HoverEvent.ShowText(state.getBlock().getName()))));
		}

		if (!(be instanceof CommandBlockBlockEntity cbe)) {
			return type;
		}

		if (cbe.isAuto()) {
			type.append(ALWAYS_ACTIVE_EMOJI);
		} else if (cbe.isPowered()) {
			type.append(POWERED_EMOJI);
		} else {
			type.append(NEEDS_REDSTONE_EMOJI);
		}

		if (cbe.isConditionalCommandBlock()) {
			type.append(CONDITIONAL_EMOJI);
		} else {
			type.append(UNCONDITIONAL_EMOJI);
		}

		return type;
	}

	static Text toBlame(final BlockEntity blockEntity) {
		final MutableText location = TextUtil.ofLocationWithTeleport(
			blockEntity.getWorld(),
			blockEntity.getPos()
		);

		if (blockEntity instanceof CommandBE cbe) {
			location.styled(style -> style.withHoverEvent(cbe.fireblanket$getBlame()));
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
	static MutableText toEmojiSpeak(final Entity e) {
		final MutableText type = Text.empty();

		if (!(e instanceof CommandBlockMinecartEntity cbe)) {
			return type.append(UNKNOWN_EMOJI.copy().styled(style -> style.withHoverEvent(
				new HoverEvent.ShowEntity(new HoverEvent.EntityContent(e.getType(), e.getUuid(), e.getName()))
			)));
		}

		type.append(MINECART_EMOJI);

		// We could've possibly caught it mid-tick, use `<=` instead of `<`.
		if (cbe.age - ((CommandBlockMinecartEntityAccessor) cbe).getLastExecuted() <= 4) {
			type.append(POWERED_EMOJI);
		} else {
			type.append(NEEDS_REDSTONE_EMOJI);
		}

		return type;
	}

	static Text toBlame(final Entity entity) {
		final MutableText location = TextUtil.ofLocation(entity.getBlockPos())
			.styled(style -> style.withClickEvent(TextUtil.toClickToTeleport(entity)));

		if (entity instanceof CommandBE cbe) {
			location.styled(style -> style.withHoverEvent(cbe.fireblanket$getBlame()));
		}

		return ExecutorUtils.toEmojiSpeak(entity)
			.append(" @ ")
			.append(location);
	}
}
