package dev.glatur.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;

public final class InvinsProtectionService {
    private static final float MINIMUM_HEALTH = 1.0F;

    private final InvinsManager invinsManager;
    private final Logger logger;

    public InvinsProtectionService(InvinsManager invinsManager, Logger logger) {
        this.invinsManager = invinsManager;
        this.logger = logger;
    }

    public boolean allowDeath(LivingEntity entity, DamageSource source, float damageAmount) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true;
        }

        if (!invinsManager.contains(player.getUuid())) {
            return true;
        }

        player.setHealth(MINIMUM_HEALTH);
        logger.info("Prevented death for tracked /invins player {}", player.getName().getString());
        return false;
    }

    public void enforceHealthClamp(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!invinsManager.contains(player.getUuid())) {
                continue;
            }

            if (!player.isAlive()) {
                continue;
            }

            if (player.getHealth() < MINIMUM_HEALTH) {
                player.setHealth(MINIMUM_HEALTH);
            }
        }
    }
}
