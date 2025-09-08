package net.modfest.fireblanket.mixin.client.annoyances.adventure;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.type.BlockPredicatesComponent;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Ampflower
 **/
@Mixin(BlockPredicatesComponent.class)
public class MixinBlockPredicatesComponent {
	@Shadow
	@Final
	private List<BlockPredicate> predicates;

	@Unique
	private static final Style DARK_GRAY = Style.EMPTY.withColor(Formatting.DARK_GRAY);
	@Unique
	private static final int PAGE_SIZE = 5;
	@Unique
	private static final boolean trap = FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT;

	@Unique
	private List<Text> friendlierTooltip;
	@Unique
	private long count = -1;

	@ModifyReturnValue(method = "getOrCreateTooltipText", at = @At("RETURN"))
	private List<Text> fireblanket$maybeReplaceTooltipText(final List<Text> original) {
		if (!trap && count > 0) {
			// Warning: client-only. Do not call on the server.
			return fireblanket$clientOnly$replaceAndPageTooltipText(original);
		}
		// Intentionally passing through as a fail-state.
		return original;
	}

	// Annoyingly, we don't have any way to avoid the problem of "are we on the client?"
	// with a simple World.isClient check, as the context is too far removed to be relevant.
	//
	// To avoid a misport from crashing the server, separate this into its own method to take advantage
	// of the JVM's laziness, and wrap the call in a 'trap or try'. Pretty nasty code smell at the end of it,
	// but this is the limitation of Mojang's current architecture.
	//
	// Why check the client? We're using `shift` to use the original list instead of the friendly list,
	// allowing one to check all of what it could be placed on without taking up the entire screen.
	// The server and any recipe viewers don't need to have a paginated view, so, ideally, they'd be left alone.
	@Unique
	private List<Text> fireblanket$clientOnly$replaceAndPageTooltipText(final List<Text> original) {
		final MinecraftClient client = MinecraftClient.getInstance();

		// Avoid modifying vanilla output while not in a screen, or on the server.
		if (!client.isOnThread() || client.currentScreen == null) {
			return original;
		}

		// TODO: wire in control to the player to adjust the page time.
		// Currently set to 2.5 seconds for not too unreasonable speed.
		if (Screen.hasShiftDown()) {
			return fireblanket$page(original, 2500);
		}

		if (this.friendlierTooltip == null) {
			this.friendlierTooltip = fireblanket$makeFriendlierTooltip(predicates);
		}

		return fireblanket$page(friendlierTooltip, 2500);
	}

	@Unique
	private static List<Text> fireblanket$page(final List<Text> texts, final int time) {
		if (texts.size() <= 10) {
			return texts;
		}

		final int pages = texts.size() / PAGE_SIZE + 1;

		final int page = (int) ((System.currentTimeMillis() / time) % pages);

		return texts.subList(page * PAGE_SIZE, Math.min(page * PAGE_SIZE + PAGE_SIZE, texts.size()));
	}

	@Inject(method = "addTooltips", at = @At("HEAD"))
	private void fireblanket$injectCount(final Consumer<Text> adder, final CallbackInfo ci) {
		if (this.count == -1) {
			this.count = this.predicates.stream()
				.flatMap(predicate -> predicate.blocks().orElseThrow().stream())
				.map(RegistryEntry::value)
				.distinct()
				.count();
		}
		if (this.count > 1) {
			adder.accept(Text.translatable("fireblanket.item.canUse.count", count));
		}
	}

	@Unique
	private static List<Text> fireblanket$makeFriendlierTooltip(final List<BlockPredicate> predicates) {
		return predicates.stream()
			.flatMap(predicate -> predicate
				.blocks()
				.orElseThrow()
				.getStorage()
				.map(
					tag -> Stream.of(Text.translatable("tag.block." + tag.id().getNamespace() + "." + tag.id().getPath())),
					blocks -> blocks.stream().map(entry -> Text.translatable(entry.value().getTranslationKey()))
				)
			)
			.map(text -> (Text) text.setStyle(DARK_GRAY))
			.toList();
	}

}

