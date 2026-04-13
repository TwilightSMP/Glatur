package dev.glatur.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import dev.glatur.config.GlaturSettings;
import dev.glatur.core.InvinsManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class InvinsCommand {
    private InvinsCommand() {
    }

    public static void register(
            CommandDispatcher<ServerCommandSource> dispatcher,
            InvinsManager invinsManager,
            Runnable saveCallback,
            Logger logger
    ) {
        dispatcher.register(
                CommandManager.literal("invins")
                        .requires(source -> source.hasPermissionLevel(GlaturSettings.COMMAND_PERMISSION_LEVEL))
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(context -> addPlayer(
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                invinsManager,
                                                saveCallback,
                                                logger
                                        ))))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(context -> removePlayer(
                                                context.getSource(),
                                                EntityArgumentType.getPlayer(context, "player"),
                                                invinsManager,
                                                saveCallback,
                                                logger
                                        ))))
                        .then(CommandManager.literal("list")
                                .executes(context -> listPlayers(context.getSource(), invinsManager)))
                        .executes(context -> listPlayers(context.getSource(), invinsManager))
        );
    }

    private static int addPlayer(
            ServerCommandSource source,
            ServerPlayerEntity player,
            InvinsManager invinsManager,
            Runnable saveCallback,
            Logger logger
    ) {
        UUID playerUuid = player.getUuid();
        if (!invinsManager.add(playerUuid)) {
            source.sendError(Text.literal(player.getName().getString() + " is already in the /invins list."));
            return 0;
        }

        saveCallback.run();
        logger.info("{} added {} ({}) to /invins", source.getName(), player.getGameProfile().getName(), playerUuid);
        source.sendFeedback(() -> Text.literal("Added " + player.getName().getString() + " to /invins list."), true);
        return 1;
    }

    private static int removePlayer(
            ServerCommandSource source,
            ServerPlayerEntity player,
            InvinsManager invinsManager,
            Runnable saveCallback,
            Logger logger
    ) {
        UUID playerUuid = player.getUuid();
        if (!invinsManager.remove(playerUuid)) {
            source.sendError(Text.literal(player.getName().getString() + " is not in the /invins list."));
            return 0;
        }

        saveCallback.run();
        logger.info("{} removed {} ({}) from /invins", source.getName(), player.getGameProfile().getName(), playerUuid);
        source.sendFeedback(() -> Text.literal("Removed " + player.getName().getString() + " from /invins list."), true);
        return 1;
    }

    private static int listPlayers(ServerCommandSource source, InvinsManager invinsManager) {
        Set<UUID> snapshot = invinsManager.snapshot();
        if (snapshot.isEmpty()) {
            source.sendFeedback(() -> Text.literal("/invins list is empty."), false);
            return 1;
        }

        List<String> names = snapshot.stream()
                .map(uuid -> formatEntry(source, uuid))
                .sorted(String::compareToIgnoreCase)
                .toList();

        source.sendFeedback(
                () -> Text.literal("/invins players (" + names.size() + "): " + String.join(", ", names)),
                false
        );
        return names.size();
    }

    private static String formatEntry(ServerCommandSource source, UUID playerUuid) {
        Optional<GameProfile> profile = source.getServer().getUserCache().getByUuid(playerUuid);
        if (profile.isPresent()) {
            return profile.get().getName() + " [" + playerUuid + "]";
        }

        return playerUuid.toString();
    }
}
