package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.modfest.fireblanket.mixin.accessor.ParticleManagerAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CountParticleTypesCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandBuildContext access) {
		base.then(literal("countparticles")
			.executes(cl -> {
				Map<ParticleRenderType, Queue<Particle>> particles = ((ParticleManagerAccessor) Minecraft.getInstance().particleEngine).getParticles();
				int total = particles.values().stream().mapToInt(Collection::size).sum();
				Map<String, Integer> values = new HashMap<>();

				for (Queue<Particle> q : particles.values()) {
					for (Particle p : q) {
						String name = p.getClass().getName();
						values.compute(name, (k, v) -> v == null ? 1 : v + 1);
					}
				}

				List<Map.Entry<String, Integer>> entries = new ArrayList<>(values.entrySet());
				entries.sort(Map.Entry.comparingByValue());

				for (Map.Entry<String, Integer> entry : entries) {
					cl.getSource().sendFeedback(Component.literal(entry.getKey() + ": " + entry.getValue()));
				}
				cl.getSource().sendFeedback(Component.literal("Total: " + total));

				return 0;
			})
		);
	}
}
