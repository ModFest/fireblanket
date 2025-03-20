package net.modfest.fireblanket.client.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.modfest.fireblanket.mixin.accessor.ParticleManagerAccessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CountParticleTypesCommand {
	public static void init(LiteralArgumentBuilder<FabricClientCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("countparticles")
			.executes(cl -> {
				Map<ParticleTextureSheet, Queue<Particle>> particles = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).getParticles();
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
					cl.getSource().sendFeedback(Text.literal(entry.getKey() + ": " + entry.getValue()));
				}
				cl.getSource().sendFeedback(Text.literal("Total: " + total));

				return 0;
			})
		);
	}
}
