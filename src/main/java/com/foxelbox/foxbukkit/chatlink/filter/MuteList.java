/**
 * This file is part of FoxBukkitChatLink.
 *
 * FoxBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
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
