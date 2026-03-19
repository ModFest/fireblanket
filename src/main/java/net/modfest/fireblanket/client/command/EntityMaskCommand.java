package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.modfest.fireblanket.client.ClientState;

import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class EntityMaskCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("entity")
			.then(literal("add")
				.then(argument("type", new EntityTypeArgumentType(access))
					.executes(client -> {
						Holder.Reference<EntityType<?>> type = getRegistryEntry(
							client,
							"type",
							Registries.ENTITY_TYPE
						);

						if (type.unwrapKey().isEmpty()) {
							client.getSource().sendFeedback(Component.literal("This entity seems to not be registered??"));
							return 1;
						}

						client.getSource()
							.sendFeedback(Component.literal("Added " + type.key().identifier() + " to the mask."));

						Minecraft.getInstance().submit(() -> ClientState.MASKED_ENTITIES.add(type.value()));
						return 0;
					})
				)
			)
			.then(literal("remove")
				.then(argument("type", new EntityTypeArgumentType(access))
					.executes(client -> {
						Holder.Reference<EntityType<?>> type = getRegistryEntry(
							client,
							"type",
							Registries.ENTITY_TYPE
						);

						if (type.unwrapKey().isEmpty()) {
							client.getSource().sendFeedback(Component.literal("This entity seems to not be registered??"));
							return 1;
						}

						client.getSource()
							.sendFeedback(Component.literal("Removed " + type.key().identifier() + " to the mask."));

						Minecraft.getInstance().submit(() -> ClientState.MASKED_ENTITIES.remove(type.value()));
						return 0;
					})
				)
			)
			.then(literal("list")
				.executes(client -> {
					if (ClientState.MASKED_ENTITIES.isEmpty()) {
						client.getSource().sendFeedback(Component.literal("Your mask is empty."));
					} else {
						String string = ClientState.MASKED_ENTITIES.stream()
							.map(t -> BuiltInRegistries.ENTITY_TYPE.getKey(t).toString())
							.collect(Collectors.joining(", "));
						client.getSource().sendFeedback(Component.literal("You are currently masking: " + string));
					}
					return 0;
				})
			)
			.then(literal("clear")
				.executes(client -> {
					int size = ClientState.MASKED_ENTITIES.size();
					Minecraft.getInstance().submit(ClientState.MASKED_ENTITIES::clear);

					client.getSource()
						.sendFeedback(Component.literal("Cleared " + size + " entit" + (size == 1 ?
							"y" :
							"ies") + " out of the mask."));
					return 0;
				})
			)
		);
	}

	private static final Dynamic3CommandExceptionType WRONG_TYPE_EXCEPTION = new Dynamic3CommandExceptionType(
		(tag, type, expectedType) -> Component.translatable("argument.resource_tag.invalid_type", tag, type, expectedType)
	);

	public static <T> Holder.Reference<T> getRegistryEntry(
		CommandContext<FabricClientCommandSource> context,
		String name,
		ResourceKey<Registry<T>> registryRef
	) throws CommandSyntaxException {
		Holder.Reference<T> reference = context.getArgument(name, Holder.Reference.class);
		ResourceKey<?> registryKey = reference.key();
		if (registryKey.isFor(registryRef)) {
			return reference;
		} else {
			throw WRONG_TYPE_EXCEPTION.create(
				registryKey.identifier(),
				registryKey.registry(),
				registryRef.identifier()
			);
		}
	}

	public static class EntityTypeArgumentType extends ResourceOrIdArgument<EntityType<?>> {

		protected EntityTypeArgumentType(CommandBuildContext registryAccess) {
			super(registryAccess, Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.byNameCodec());
		}
	}
}
