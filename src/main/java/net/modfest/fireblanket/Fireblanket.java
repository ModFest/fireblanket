package net.modfest.fireblanket;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.command.DumpCommandBlocksCommand;
import net.modfest.fireblanket.command.DumpEntityTypesCommand;
import net.modfest.fireblanket.mixin.ClientConnectionAccessor;
import net.modfest.fireblanket.world.blocks.UpdateSignBlockEntityTypes;
import net.modfest.fireblanket.world.entity.EntityFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Fireblanket implements ModInitializer {
	public static final Identifier BATCHED_BE_UPDATE = new Identifier("fireblanket", "batched_be_sync");
    public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");
    
    public record QueuedPacket(ClientConnection conn, Packet<?> packet, PacketCallbacks listener) {}
    
    private static final AtomicInteger nextQueue = new AtomicInteger();
    
    @SuppressWarnings("unchecked")
	public static final LinkedBlockingQueue<QueuedPacket>[] PACKET_QUEUES = new LinkedBlockingQueue[4];

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
		
		for (int i = 0; i < PACKET_QUEUES.length; i++) {
			LinkedBlockingQueue<QueuedPacket> q = new LinkedBlockingQueue<>();
			PACKET_QUEUES[i] = q;
			Thread thread = new Thread(() -> {
				while (true) {
					try {
						QueuedPacket p = q.take();
						((ClientConnectionAccessor)p.conn()).fireblanket$sendImmediately(p.packet(), p.listener());
					} catch (Throwable t) {
						LOGGER.error("Exception in packet thread", t);
					}
				}
			}, "Fireblanket async packet send thread #"+(i+1));
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static LinkedBlockingQueue<QueuedPacket> getNextQueue() {
		return PACKET_QUEUES[Math.floorMod(nextQueue.getAndIncrement(), PACKET_QUEUES.length)];
	}
}
