package net.modfest.fireblanket;

import net.fabricmc.api.ModInitializer;

import net.modfest.fireblanket.command.DumpCommandBlocksCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fireblanket implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Fireblanket");

	@Override
	public void onInitialize() {
		DumpCommandBlocksCommand.init();
	}
}