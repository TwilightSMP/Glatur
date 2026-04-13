package dev.glatur.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class InvinsManager {
    private final Set<UUID> trackedPlayers = new HashSet<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public boolean add(UUID playerUuid) {
        lock.writeLock().lock();
        try {
            return trackedPlayers.add(playerUuid);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean remove(UUID playerUuid) {
        lock.writeLock().lock();
        try {
            return trackedPlayers.remove(playerUuid);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean contains(UUID playerUuid) {
        lock.readLock().lock();
        try {
            return trackedPlayers.contains(playerUuid);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void replaceAll(Collection<UUID> playerUuids) {
        lock.writeLock().lock();
        try {
            trackedPlayers.clear();
            trackedPlayers.addAll(playerUuids);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Set<UUID> snapshot() {
        lock.readLock().lock();
        try {
            return new HashSet<>(trackedPlayers);
        } finally {
            lock.readLock().unlock();
        }
    }
}
