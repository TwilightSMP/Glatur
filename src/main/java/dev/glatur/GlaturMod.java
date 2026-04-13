package dev.glatur;

import dev.glatur.command.InvinsCommand;
import dev.glatur.config.GlaturSettings;
import dev.glatur.core.InvinsManager;
import dev.glatur.core.InvinsProtectionService;
import dev.glatur.storage.InvinsStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public final class GlaturMod implements ModInitializer {
    public static final String MOD_ID = "glatur";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private final InvinsManager invinsManager = new InvinsManager();
    private final InvinsStorage invinsStorage = new InvinsStorage(LOGGER);
    private final InvinsProtectionService protectionService = new InvinsProtectionService(invinsManager, LOGGER);

    private Path storagePath;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Glatur mod");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                InvinsCommand.register(dispatcher, invinsManager, this::saveInvinsList, LOGGER)
        );

        ServerLivingEntityEvents.ALLOW_DEATH.register(protectionService::allowDeath);
        ServerTickEvents.END_SERVER_TICK.register(protectionService::enforceHealthClamp);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            storagePath = GlaturSettings.resolveStoragePath(server);
            Set<UUID> loadedPlayers = invinsStorage.load(storagePath);
            invinsManager.replaceAll(loadedPlayers);
            LOGGER.info("Loaded {} /invins player(s) from {}", loadedPlayers.size(), storagePath);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            saveInvinsList();
            LOGGER.info("Glatur shutdown complete");
        });
    }

    private void saveInvinsList() {
        if (storagePath == null) {
            LOGGER.warn("Skipping /invins save because storage path has not been initialized yet");
            return;
        }

        Set<UUID> snapshot = invinsManager.snapshot();
        invinsStorage.save(storagePath, snapshot);
        LOGGER.info("Saved {} /invins player(s) to {}", snapshot.size(), storagePath);
    }
}
