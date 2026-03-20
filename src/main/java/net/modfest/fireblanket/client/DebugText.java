package net.modfest.fireblanket.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * @author Ampflower
 **/
public final class DebugText {
	public static final Component renderRegionVisualizeOff = Component.translatable("fireblanket.debug.render_region.visualize.off");
	public static final Component renderRegionVisualizeOn = Component.translatable("fireblanket.debug.render_region.visualize.on");
	public static final Component renderRegionRenderingOff = Component.translatable("fireblanket.debug.render_region.rendering.off");
	public static final Component renderRegionRenderingOn = Component.translatable("fireblanket.debug.render_region.rendering.on");

	public static final Component tickTimesOff = Component.translatable("fireblanket.debug.tick_times.off");
	public static final Component tickTimesOn = Component.translatable("fireblanket.debug.tick_times.on");
	public static final Component blockTickTimesOff = Component.translatable("fireblanket.debug.block_tick_times.off");
	public static final Component blockTickTimesOn = Component.translatable("fireblanket.debug.block_tick_times.on");
	public static final Component entityTickTimesOff = Component.translatable("fireblanket.debug.entity_tick_times.off");
	public static final Component entityTickTimesOn = Component.translatable("fireblanket.debug.entity_tick_times.on");

	public static final Component tickTimesAdvancedWarning = Component.translatable("fireblanket.debug.tick_times.advanced_warning");

	private static Component decorateDebug(final ChatFormatting formatting, final Component component) {
		return Component.empty()
			.append("[")
			.append(Component.literal("Fireblanket").withStyle(formatting, ChatFormatting.BOLD))
			.append("] ")
			.append(component);
	}

	public static Component debugWarning(final Component component) {
		return decorateDebug(ChatFormatting.RED, component);
	}

	public static Component debugFeedback(final Component component) {
		return decorateDebug(ChatFormatting.YELLOW, component);
	}

	public static Component debugFeedbackWithFallback(final String key, final String fallback, final Object... args) {
		return decorateDebug(ChatFormatting.YELLOW, Component.translatableWithFallback(key, fallback, args));
	}
}
