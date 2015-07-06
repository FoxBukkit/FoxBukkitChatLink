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
package com.foxelbox.foxbukkit.chatlink.bans;

import java.net.InetAddress;
import java.sql.Date;

public class LogEntry {
    private String action;
    private Date time;
    private InetAddress ip;
    private int player;
    private String server;

    protected LogEntry(String action, Date time, InetAddress ip, int player, String server) {
        this.action = action;
        this.time = time;
        this.ip = ip;
        this.player = player;
        this.server = server;
    }

    public BanPlayer getPlayer() {
        return BanResolver.getUserByID(player);
    }

    public String getAction() {
        return action;
    }

    public Date getTime() {
        return time;
    }

    public InetAddress getIp() {
        return ip;
    }

    public String getServer() {
        return server;
    }
}
