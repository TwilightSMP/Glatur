package dev.glatur.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
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

        // Let vanilla handle lethal hits if a totem is available so the totem can pop normally.
        if (hasTotemOfUndying(player)) {
            return true;
        }

        player.setHealth(MINIMUM_HEALTH);
        logger.info("Prevented death for tracked /invins player {}", player.getName().getString());
        return false;
    }

    private boolean hasTotemOfUndying(ServerPlayerEntity player) {
        return player.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING)
                || player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
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
