package net.aetheris.client;

import net.fabricmc.api.ClientModInitializer;
import net.aetheris.client.modules.ModuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AetherisClient implements ClientModInitializer {
    public static final String MOD_ID = "aetheris-core";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Aetheris Client 1.21.4");
        ModuleManager.init();
    }
}
