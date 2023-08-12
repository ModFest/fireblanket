package net.modfest.fireblanket;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.modfest.fireblanket.command.DumpCommandBlocksCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fireblanket implements ModInitializer {
	public static final Identifier BATCHED_BE_UPDATE = new Identifier("fireblanket", "batched_be_sync");
    public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");

	@Override
	public void onInitialize() {
		DumpCommandBlocksCommand.init();
	}
}