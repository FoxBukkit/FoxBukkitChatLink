package de.doridian.yiffbukkit.chatlink;

import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;

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
}
