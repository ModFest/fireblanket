package net.modfest.fireblanket;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.command.DumpCommandBlocksCommand;
import net.modfest.fireblanket.command.DumpEntityTypesCommand;
import net.modfest.fireblanket.world.entity.EntityFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

public class Fireblanket implements ModInitializer {
	public static final Identifier BATCHED_BE_UPDATE = new Identifier("fireblanket", "batched_be_sync");
    public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");

	@Override
	public void onInitialize() {
		DumpCommandBlocksCommand.init();

		DumpEntityTypesCommand.init();

		Path configs = FabricLoader.getInstance().getConfigDir().resolve("fireblanket");
		if (Files.exists(configs)) {
			Path types = configs.resolve("entityfilters.txt");

			if (Files.exists(types)) {
				EntityFilters.parse(types);
			}
		}
	}
}