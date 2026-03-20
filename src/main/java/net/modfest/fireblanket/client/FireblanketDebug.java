package net.modfest.fireblanket.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.modfest.fireblanket.FireblanketConstants;

import java.util.function.Consumer;

/**
 * @author Ampflower
 */
public final class FireblanketDebug {
	public static final Identifier RENDER_REGION_VISUALIZE = DebugScreenEntries.register(FireblanketConstants.id("render_region_visualize"), new DebugEntryNoop());
	public static final Identifier ENTITY_TICK_TIMES = DebugScreenEntries.register(FireblanketConstants.id("entity_tick_times"), new DebugEntryNoop());
	public static final Identifier BLOCK_TICK_TIMES = DebugScreenEntries.register(FireblanketConstants.id("block_tick_times"), new DebugEntryNoop());

	private static final KeyMapping.Category DEBUG_KEY_CATEGORY = KeyMapping.Category.register(FireblanketConstants.id("debug"));

	private static final KeyMapping renderRegionVisualizeKey = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.fireblanket.debug.render_region.visualize", InputConstants.UNKNOWN.getValue(), DEBUG_KEY_CATEGORY));
	private static final KeyMapping renderRegionRenderingKey = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.fireblanket.debug.render_region.toggle", InputConstants.UNKNOWN.getValue(), DEBUG_KEY_CATEGORY));
	private static final KeyMapping tickTimesKey = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.fireblanket.debug.tick_times", InputConstants.UNKNOWN.getValue(), DEBUG_KEY_CATEGORY));
	private static final KeyMapping entityTickTimesKey = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.fireblanket.debug.entity_tick_times", InputConstants.UNKNOWN.getValue(), DEBUG_KEY_CATEGORY));
	private static final KeyMapping blockTickTimesKey = KeyMappingHelper.registerKeyMapping(
		new KeyMapping("key.fireblanket.debug.block_tick_times", InputConstants.UNKNOWN.getValue(), DEBUG_KEY_CATEGORY));

	private static boolean advancedTimingWarningShown = false;

	public static void init() {
		ClientTickEvents.START_LEVEL_TICK.register(_ -> {
			final DebugScreenEntryList debugEntries = Minecraft.getInstance().debugEntries;
			ClientState.displayingEntityTickTimes = debugEntries.isCurrentlyEnabled(ENTITY_TICK_TIMES);
			ClientState.displayingBlockTickTimes = debugEntries.isCurrentlyEnabled(BLOCK_TICK_TIMES);

			if (!advancedTimingWarningShown && (ClientState.displayingEntityTickTimes || ClientState.displayingBlockTickTimes)) {
				showAdvancedTimingWarning();
			}
		});
	}


	public static void toggleTickTimes(Consumer<Component> feedback) {
		final DebugScreenEntryList debugEntries = Minecraft.getInstance().debugEntries;
		if (debugEntries.isCurrentlyEnabled(FireblanketDebug.BLOCK_TICK_TIMES) || debugEntries.isCurrentlyEnabled(FireblanketDebug.ENTITY_TICK_TIMES)) {
			debugEntries.setStatus(FireblanketDebug.BLOCK_TICK_TIMES, DebugScreenEntryStatus.NEVER);
			debugEntries.setStatus(FireblanketDebug.ENTITY_TICK_TIMES, DebugScreenEntryStatus.NEVER);
			feedback.accept(DebugText.tickTimesOff);
		} else {
			debugEntries.toggleStatus(FireblanketDebug.BLOCK_TICK_TIMES);
			debugEntries.toggleStatus(FireblanketDebug.ENTITY_TICK_TIMES);
			feedback.accept(DebugText.tickTimesOn);
			showAdvancedTimingWarning(feedback);
		}
	}

	public static boolean handleDebugKeys(final Minecraft minecraft, final KeyEvent keyEvent) {
		boolean takenAction = false;

		if (renderRegionVisualizeKey.matches(keyEvent)) {
			if (minecraft.debugEntries.toggleStatus(RENDER_REGION_VISUALIZE)) {
				sendFeedback(minecraft, DebugText.renderRegionVisualizeOn);
			} else {
				sendFeedback(minecraft, DebugText.renderRegionVisualizeOff);
			}
			takenAction = true;
		}

		if (renderRegionRenderingKey.matches(keyEvent)) {
			if (ClientState.useRegionRenderer) {
				sendFeedback(minecraft, DebugText.renderRegionRenderingOff);
				ClientState.useRegionRenderer = false;
			} else {
				sendFeedback(minecraft, DebugText.renderRegionRenderingOn);
				ClientState.useRegionRenderer = true;
			}
			takenAction = true;
		}

		if (tickTimesKey.matches(keyEvent)) {
			toggleTickTimes(feedback -> sendFeedback(Minecraft.getInstance(), feedback));
			takenAction = true;
		}

		if (blockTickTimesKey.matches(keyEvent)) {
			if (minecraft.debugEntries.toggleStatus(BLOCK_TICK_TIMES)) {
				sendFeedback(minecraft, DebugText.blockTickTimesOn);
				showAdvancedTimingWarning();
			} else {
				sendFeedback(minecraft, DebugText.blockTickTimesOff);
			}
			takenAction = true;
		}

		if (entityTickTimesKey.matches(keyEvent)) {
			if (minecraft.debugEntries.toggleStatus(ENTITY_TICK_TIMES)) {
				sendFeedback(minecraft, DebugText.entityTickTimesOn);
				showAdvancedTimingWarning();
			} else {
				sendFeedback(minecraft, DebugText.entityTickTimesOff);
			}
			takenAction = true;
		}

		return takenAction;
	}

	private static void send(final Minecraft minecraft, final Component component) {
		minecraft.getChatListener().handleSystemMessage(component, false);
	}

	private static void sendWarning(final Minecraft minecraft, final Component component) {
		send(minecraft, DebugText.debugWarning(component));
	}

	private static void sendFeedback(final Minecraft minecraft, final Component component) {
		send(minecraft, DebugText.debugFeedback(component));
	}

	private static void showAdvancedTimingWarning() {
		advancedTimingWarningShown = true;
		sendWarning(Minecraft.getInstance(), DebugText.tickTimesAdvancedWarning);
	}

	private static void showAdvancedTimingWarning(Consumer<Component> consumer) {
		advancedTimingWarningShown = true;
		consumer.accept(DebugText.tickTimesAdvancedWarning);
	}
}
