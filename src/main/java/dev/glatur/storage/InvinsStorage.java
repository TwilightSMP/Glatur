package dev.glatur.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class InvinsStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int SCHEMA_VERSION = 1;

    private final Logger logger;

    public InvinsStorage(Logger logger) {
        this.logger = logger;
    }

    public Set<UUID> load(Path storagePath) {
        if (Files.notExists(storagePath)) {
            logger.info("/invins storage file does not exist yet: {}", storagePath);
            return Collections.emptySet();
        }

        try (Reader reader = Files.newBufferedReader(storagePath, StandardCharsets.UTF_8)) {
            StorageModel model = GSON.fromJson(reader, StorageModel.class);
            if (model == null || model.invinsPlayers == null) {
                logger.warn("/invins storage was empty or invalid JSON object: {}", storagePath);
                return Collections.emptySet();
            }

            if (model.schemaVersion != SCHEMA_VERSION) {
                logger.warn("/invins storage schema version {} differs from expected {}", model.schemaVersion, SCHEMA_VERSION);
            }

            Set<UUID> loaded = new HashSet<>();
            for (String rawUuid : model.invinsPlayers) {
                try {
                    loaded.add(UUID.fromString(rawUuid));
                } catch (IllegalArgumentException exception) {
                    logger.warn("Skipping invalid UUID in /invins storage: {}", rawUuid);
                }
            }

            return loaded;
        } catch (Exception exception) {
            handleCorruptStorage(storagePath, exception);
            return Collections.emptySet();
        }
    }

    public void save(Path storagePath, Set<UUID> playerUuids) {
        try {
            if (storagePath.getParent() != null) {
                Files.createDirectories(storagePath.getParent());
            }

            List<String> serializedUuids = playerUuids.stream()
                    .map(UUID::toString)
                    .sorted()
                    .toList();

            StorageModel model = new StorageModel(SCHEMA_VERSION, serializedUuids);

            Path tempPath = storagePath.resolveSibling(storagePath.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(
                    tempPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                GSON.toJson(model, writer);
            }

            try {
                Files.move(tempPath, storagePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(tempPath, storagePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            logger.error("Failed to save /invins storage at {}", storagePath, exception);
        }
    }

    private void handleCorruptStorage(Path storagePath, Exception exception) {
        logger.error("Failed to load /invins storage from {}. Starting with an empty list.", storagePath, exception);

        String backupName = storagePath.getFileName() + ".corrupt." + Instant.now().toEpochMilli();
        Path backupPath = storagePath.resolveSibling(backupName);

        try {
            Files.move(storagePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            logger.warn("Moved corrupt /invins storage to {}", backupPath);
        } catch (Exception moveException) {
            logger.error("Could not move corrupt /invins storage file {}", storagePath, moveException);
        }
    }

    private static final class StorageModel {
        private int schemaVersion;
        private List<String> invinsPlayers;

        private StorageModel() {
        }

        private StorageModel(int schemaVersion, List<String> invinsPlayers) {
            this.schemaVersion = schemaVersion;
            this.invinsPlayers = invinsPlayers;
        }
    }
}
