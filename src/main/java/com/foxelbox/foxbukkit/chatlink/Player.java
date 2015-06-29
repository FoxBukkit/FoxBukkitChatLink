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
package com.foxelbox.foxbukkit.chatlink;

import com.foxelbox.dependencies.redis.RedisManager;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.UserInfo;
import com.foxelbox.foxbukkit.chatlink.permissions.FoxBukkitPermissionHandler;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.UUID;

public class Player {
    private final UUID uuid;
    private final String name;
    private String displayName;

    public boolean isMuted;

    public static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes("COMMANDSENDER:CONSOLE".getBytes());

    public static Player getPlayerFromMessage(ChatMessageOut message) {
        if(message.from.uuid == null || message.from.uuid.equals(CONSOLE_UUID))
            return new ConsolePlayer(message.from.name);
        return new Player(message.from.uuid, message.from.name);
    }

    public static Player getPlayerFromMessage(ChatMessageIn message) {
        if(message.from.uuid == null || message.from.uuid.equals(CONSOLE_UUID))
            return new ConsolePlayer(message.from.name);
        return new Player(message.from.uuid, message.from.name);
    }

    public Player(UUID uuid) {
        this(uuid, PlayerHelper.playerUUIDToName.get(uuid.toString()));
    }

    public Player(Player ply) {
        this(ply.uuid);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        if(uuid != null) {
            final String nick = PlayerHelper.getPlayerNick(uuid);
            this.displayName = (nick != null) ? nick : name;
        }
    }

    public void kick(String reason) {
        ChatMessageOut messageOut = new ChatMessageOut(null, new UserInfo(uuid, name));
        messageOut.contents = "[Kicked] " + reason;
        messageOut.type = "kick";
        messageOut.to.type = "player";
        messageOut.to.filter = new String[] { uuid.toString() };
        RedisHandler.sendMessage(messageOut);
        showKickMessage(reason);
    }

    public void chat(String message) {
        ChatMessageOut messageOut = new ChatMessageOut(null, new UserInfo(uuid, name));
        messageOut.contents = message;
        messageOut.type = "inject";
        messageOut.to.type = "player";
        messageOut.to.filter = new String[] { uuid.toString() };
        RedisHandler.sendMessage(messageOut);
    }

    public void showKickMessage(String reason) {
        if (isOnline()) {
            ChatMessageOut messageOut = new ChatMessageOut(null, new UserInfo(uuid, name));
            messageOut.setContents(RedisHandler.KICK_FORMAT, new String[]{
                    name, uuid.toString(), PlayerHelper.getFullPlayerName(uuid, name), reason
            });
            RedisHandler.sendMessage(messageOut);
        }
    }

    public boolean hasPermission(String permission) {
        return FoxBukkitPermissionHandler.instance.has(this, permission);
    }

    public int getLevel() {
        return PlayerHelper.getPlayerLevel(getUniqueId());
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
