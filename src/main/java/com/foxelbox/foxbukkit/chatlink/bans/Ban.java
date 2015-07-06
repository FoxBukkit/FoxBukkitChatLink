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
/**
 * This file is part of FoxBukkit.
 *
 * FoxBukkit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FoxBukkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FoxBukkit.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foxelbox.foxbukkit.chatlink.bans;

import java.sql.Date;
import java.util.UUID;

public class Ban {
	private String reason;
	private int admin;
	private int player;
	private String type;
	private Date time;

	protected final long retrievalTime;

	protected Ban(String reason, int admin, int player, String type, Date time) {
		this.reason = reason;
		this.admin = admin;
		this.player = player;
		this.type = type;
		this.time = time;
		this.retrievalTime = System.currentTimeMillis();
	}

	public Ban() {
		this.retrievalTime = System.currentTimeMillis();
		refreshTime();
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getAdminID() {
		return admin;
	}

	public int getPlayerID() {
		return player;
	}

	public BanPlayer getAdmin() {
		return BanResolver.getUserByID(admin);
	}

	public BanPlayer getPlayer() {
		return BanResolver.getUserByID(player);
	}

	public void setPlayer(String username, UUID uuid) {
		this.player = BanResolver.getUserID(username, uuid, true);
	}

	public void setAdmin(String adminname, UUID uuid) {
		this.admin = BanResolver.getUserID(adminname, uuid, true);
	}

	public void setPlayer(int player) {
		this.player = player;
	}

	public void setAdmin(int admin) {
		this.admin = admin;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void refreshTime() {
		this.time = new Date(new java.util.Date().getTime());
	}

	public Date getTime() {
		return time;
	}
}
