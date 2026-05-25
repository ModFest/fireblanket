package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.modfest.fireblanket.client.ClientState;
import net.modfest.fireblanket.util.RegistryUtil;

import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class BERMaskCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("be")
			.then(literal("add")
				.then(argument("type", StringArgumentType.greedyString())
					.suggests(ResourceOrTagArgument.resourceOrTag(
						access,
						Registries.BLOCK_ENTITY_TYPE
					)::listSuggestions)
					.executes(client -> {
						int count = 0;
						for (final Holder<BlockEntityType<?>> holder : getEntries(client, "type")) {
							if (ClientState.MASKED_BERS.add(holder.value())) {
								client.getSource().sendFeedback(
									Component.literal("Added ")
										.append(holder.unwrapKey()
											.map(ResourceKey::identifier)
											.map(Identifier::toString)
											.orElse("-- unknown --"))
										.append(" to the mask.")
								);
								count++;
							}
						}

						if (count == 0) {
							client.getSource().sendFeedback(Component.literal("Seems there was nothing to add."));
						}

						return count;
					})
				)
			)
			.then(literal("remove")
				.then(argument("type", StringArgumentType.greedyString())
					.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
						ClientState.MASKED_BERS,
						builder,
						value -> String.valueOf(ctx.getSource()
							.registryAccess()
							.lookupOrThrow(Registries.BLOCK_ENTITY_TYPE)
							.getKey(value)),
						value -> Component.literal(String.valueOf(ctx.getSource()
							.registryAccess()
							.lookupOrThrow(Registries.BLOCK_ENTITY_TYPE)
							.getKey(value)))
					))
					.executes(client -> {
						int count = 0;
						for (final Holder<BlockEntityType<?>> holder : getEntries(client, "type")) {
							if (ClientState.MASKED_BERS.remove(holder.value())) {
								client.getSource().sendFeedback(
									Component.literal("Removed ")
										.append(holder.unwrapKey()
											.map(ResourceKey::identifier)
											.map(Identifier::toString)
											.orElse("-- unknown --"))
										.append(" from the mask.")
								);
								count++;
							}
						}

						if (count == 0) {
							client.getSource().sendFeedback(Component.literal("Seems there was nothing to remove."));
						}

						return count;
					})
				)
			)
			.then(literal("list")
				.executes(client -> {
					if (ClientState.MASKED_BERS.isEmpty()) {
						client.getSource().sendFeedback(Component.literal("Your mask is empty."));
					} else {
						String string = ClientState.MASKED_BERS.stream().map(t -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(t).toString()).collect(Collectors.joining(", "));
						client.getSource().sendFeedback(Component.literal("You are currently masking: [" + string + "]"));
					}
					return 0;
				})
			)
			.then(literal("clear")
				.executes(client -> {
					int size = ClientState.MASKED_BERS.size();
					Minecraft.getInstance().submit(ClientState.MASKED_BERS::clear);

					client.getSource().sendFeedback(Component.literal("Cleared " + size + " BE" + (size == 1 ? "" : "s") + " out of the mask."));
					return 0;
				})
			)
		);
	}

	private static Iterable<? extends Holder<BlockEntityType<?>>> getEntries(
		CommandContext<FabricClientCommandSource> context,
		String name
	) {
		return RegistryUtil.getResources(
			context.getSource().registryAccess(),
			Registries.BLOCK_ENTITY_TYPE,
			StringArgumentType.getString(context, name)
		);
	}
}
