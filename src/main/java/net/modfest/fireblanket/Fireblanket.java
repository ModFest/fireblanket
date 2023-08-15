package net.modfest.fireblanket;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.command.DumpCommandBlocksCommand;
import net.modfest.fireblanket.command.DumpEntityTypesCommand;
import net.modfest.fireblanket.world.blocks.UpdateSignBlockEntityTypes;
import net.modfest.fireblanket.world.entity.EntityFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;

public class Fireblanket implements ModInitializer {
	public static final Identifier BATCHED_BE_UPDATE = new Identifier("fireblanket", "batched_be_sync");
    public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");
    
    public static final LinkedBlockingQueue<Runnable> PACKET_QUEUE = new LinkedBlockingQueue<>();

	@Override
	public void onInitialize() {
		DumpCommandBlocksCommand.init();
		DumpEntityTypesCommand.init();

		for (Block block : Registries.BLOCK) {
			UpdateSignBlockEntityTypes.apply(block);
		}

		RegistryEntryAddedCallback.event(Registries.BLOCK).register((r, id, block) -> {
			UpdateSignBlockEntityTypes.apply(block);
		});

		Path configs = FabricLoader.getInstance().getConfigDir().resolve("fireblanket");
		if (Files.exists(configs)) {
			Path types = configs.resolve("entityfilters.txt");

			if (Files.exists(types)) {
				EntityFilters.parse(types);
			}
		}
		
		Thread packetThread = new Thread(() -> {
			while (true) {
				try {
					PACKET_QUEUE.take().run();
				} catch (Throwable t) {
					LOGGER.error("Exception in packet thread", t);
				}
			}
		}, "Fireblanket async packet send thread");
		packetThread.setDaemon(true);
		packetThread.start();
	}
}