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

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.UUID;

public class Bans {
    public static Bans instance = new Bans();

	private Bans() {

	}


	public enum BanType {
		GLOBAL("global"), LOCAL("local"), TEMPORARY("temp");

		private final String name;

		BanType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	public void unban(final Player from, final String ply) {
		new Thread() {
			public void run() {
				Ban ban = BanResolver.getBan(ply, null, false);
				if(ban != null) {
					BanResolver.deleteBan(ban);
					RedisHandler.sendSimpleMessage(from.getName() + " unbanned " + ply + "!");
				} else {
					RedisHandler.sendSimpleMessage(from, "Player with the name " + ply + " was not banned!");
				}
			}
		}.start();
	}

	public void ban(final Player from, final Player ply, final String reason, final BanType type) {
		if (type == BanType.TEMPORARY) return;
		ban(from, ply, reason, type, 0, "");
	}

    public void ban(final Player from, final Player ply, final String reason, final BanType type, final long duration, final String measure) {
        if (type == BanType.TEMPORARY) return;
        ban(from, ply.getName(), ply.getUniqueId(), reason, type, duration, measure);
    }

	public void ban(final Player from, final String plyName, final UUID plyUUID, final String reason, final BanType type) {
		if (type == BanType.TEMPORARY) return;
		ban(from, plyName, plyUUID, reason, type, 0, "");
	}

	public void ban(final Player from, final String _plyName, final UUID plyUUID, final String reason, final BanType type, final long duration, final String measure) {
		if (type == null) return;
		if (type == BanType.TEMPORARY) return;

		final String plyName;
		if(_plyName == null)
			plyName = PlayerHelper.playerUUIDToName.get(plyUUID.toString());
		else
			plyName = _plyName;

		new Thread() {
			public void run() {
				Ban newBan = new Ban();
				newBan.setUser(plyName, plyUUID);
				newBan.setAdmin(from.getName(), from.getUniqueId());
				newBan.setReason(reason);
				newBan.setType(type.getName());
				BanResolver.addBan(newBan);
				RedisHandler.sendSimpleMessage(from.getName() + " banned " + plyName + " [Reason: " + reason + "]!");
			}
		}.start();
	}
}