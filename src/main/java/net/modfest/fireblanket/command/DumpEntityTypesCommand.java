package net.modfest.fireblanket.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class DumpEntityTypesCommand {
	public static void init() {
		CommandRegistrationCallback.EVENT.register((event, access, env) -> {
			event.register(CommandManager.literal("fireblanket:dumpentities")
					.requires(source -> source.hasPermissionLevel(4))
					.executes(server -> {
						for (EntityType<?> type : Registries.ENTITY_TYPE) {
							server.getSource().sendFeedback(() -> Text.literal(Registries.ENTITY_TYPE.getId(type) + " alwaysUpdateVelocity=" + type.alwaysUpdateVelocity() + " updateDistance(blocks)=" + (type.getMaxTrackDistance() * 16) + " tickInterval=" + type.getTrackTickInterval()), false);
						}

						return 0;
					}
				)
			);
		});
	}
}
