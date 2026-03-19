package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.modfest.fireblanket.stacksmash.StackUtil;
import net.modfest.fireblanket.stacksmash.TraceLevel;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

/**
 * @author Ampflower
 **/
public final class StackTracerCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("trace")
			.then(argument("level", new TraceLevelArgumentType())
				.executes(ctx -> {
					final TraceLevel level = TraceLevelArgumentType.getTraceLevel(ctx, "level");

					if (StackUtil.getTraceLevel() == level) {
						ctx.getSource().sendFeedback(Component.nullToEmpty("Already set to " + level));
						return 0;
					}

					StackUtil.setTraceLevel(level);
					ctx.getSource().sendFeedback(Component.nullToEmpty("Set tracer to " + level));

					return 1;
				})
			)
			.executes(ctx -> {
				ctx.getSource().sendFeedback(Component.nullToEmpty("Tracer set to " + StackUtil.getTraceLevel()));

				return 1;
			})
		);
	}

	private static final class TraceLevelArgumentType extends StringRepresentableArgument<TraceLevel> {
		private static final Codec<TraceLevel> TRACE_LEVEL_CODEC = StringRepresentable.fromEnum(TraceLevel::values);

		private TraceLevelArgumentType() {
			super(TRACE_LEVEL_CODEC, TraceLevel::values);
		}

		public static TraceLevel getTraceLevel(CommandContext<?> context, String id) {
			return context.getArgument(id, TraceLevel.class);
		}
	}
}
