package com.foxelbox.foxbukkit.chatlink.filter;

import com.foxelbox.foxbukkit.chatlink.Player;

import java.util.HashSet;
import java.util.UUID;

public class MuteList {
    private final static HashSet<UUID> mutedSet = new HashSet<>();

    public static void setMuteState(Player ply, boolean muted) {
        setMuteState(ply.getUniqueId(), muted);
    }

    public static void setMuteState(UUID uuid, boolean muted) {
        synchronized (mutedSet){
            if (muted) {
                mutedSet.add(uuid);
            } else {
                mutedSet.remove(uuid);
            }
        }
    }

    public static boolean isMuted(Player ply) {
        return isMuted(ply.getUniqueId());
    }

    public static boolean isMuted(UUID uuid) {
        return mutedSet.contains(uuid);
    }
}
