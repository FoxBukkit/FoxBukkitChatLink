/**
 * This file is part of YiffBukkitChatLink.
 *
 * YiffBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.yiffbukkit.chatlink;

import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;

import java.util.List;
import java.util.UUID;

public class Player {
    public final UUID uuid;
    public final String name;
    public final String displayName;

    public Player(UUID uuid) {
        this(uuid, PlayerHelper.playerUUIDToName.get(uuid.toString()));
    }

    public Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        final String nick = PlayerHelper.getPlayerNick(uuid);
        this.displayName = (nick != null) ? nick : name;
    }

    public boolean isOnline() {
        return PlayerHelper.getOnlinePlayersOnAllServers().contains(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof Player))
            return false;
        Player player = (Player) o;
        return displayName.equals(player.displayName) && name.equals(player.name) && uuid.equals(player.uuid);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + displayName.hashCode();
        return result;
    }
}
