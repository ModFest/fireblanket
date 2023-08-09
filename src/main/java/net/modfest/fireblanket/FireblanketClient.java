package net.modfest.fireblanket;

import net.fabricmc.api.ClientModInitializer;
import net.modfest.fireblanket.client.command.BERMaskCommand;

public class FireblanketClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (GlobalFlags.DO_BE_MASKING) {
            BERMaskCommand.init();
        }
    }
}
