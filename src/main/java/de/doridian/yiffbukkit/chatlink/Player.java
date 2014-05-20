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
