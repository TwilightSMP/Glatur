package dev.glatur.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Path;

public final class GlaturSettings {
    // TODO: Confirm whether this command permission level should be different.
    public static final int COMMAND_PERMISSION_LEVEL = 2;

    private static final String MOD_DATA_DIRECTORY = "glatur";
    private static final String STORAGE_FILE_NAME = "invins.json";

    private GlaturSettings() {
    }

    public static Path resolveStoragePath(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT)
                .resolve("data")
                .resolve(MOD_DATA_DIRECTORY)
                .resolve(STORAGE_FILE_NAME);
    }
}
