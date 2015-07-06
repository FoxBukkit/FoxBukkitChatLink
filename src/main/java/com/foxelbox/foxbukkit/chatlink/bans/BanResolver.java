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

import com.foxelbox.foxbukkit.chatlink.database.DatabaseConnectionPool;

import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class BanResolver {
	private static HashMap<UUID, Integer> playerIDs = new HashMap<>();
	private static HashMap<Integer, BanPlayer> playerUUIDs = new HashMap<>();
	private static HashMap<Integer, SoftReference<Ban>> playerBans = new HashMap<>();

	private static final long BAN_MAX_AGE_MILLIS = 60 * 1000;

	public static Ban getBan(String username, UUID uuid) {
		return getBan(username, uuid, true);
	}

	public static String makePossibleAltString(String user, UUID uuid) {
		final Collection<BanPlayer> alts = getPossibleAltsForPlayer(user, uuid);
		if(alts == null || alts.isEmpty())
			return null;

		final StringBuilder sb = new StringBuilder();

		boolean notFirst = false;
		boolean hasBans = false;
		for (BanPlayer alt : alts) {
			final Ban altBan = getBan(alt.name, alt.uuid);

			if (notFirst)
				sb.append(", ");
			else
				notFirst = true;

			if (altBan != null) {
				hasBans = true;
				sb.append("\u00a7c");
			}
			else
				sb.append("\u00a7a");

			sb.append(alt.name);
		}

		if (hasBans)
			return String.format("%1$s has some banned possible alts: %2$s", user, sb);
		else
			return String.format("Possible alts of %1$s: %2$s", user, sb);
	}

	public static Collection<BanPlayer> getPossibleAltsForPlayer(String username, UUID uuid) {
		int userID = getUserID(username, uuid);
		if(userID < 1)
			return null;

		try {
			Connection connection = DatabaseConnectionPool.instance.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT player FROM player_ips WHERE ip IN (SELECT ip FROM player_ips WHERE player = ?)");
			preparedStatement.setInt(1, userID);
			ResultSet resultSet = preparedStatement.executeQuery();
			HashMap<Integer, BanPlayer> alts = new HashMap<>();
			while(resultSet.next()) {
				int player = resultSet.getInt("player");
				if(player == userID)
					continue;
				BanPlayer ply = getUserByID(player);
				if(ply == null)
					System.out.println("INVALID PLAYER #" + player);
				else
					alts.put(player, ply);
			}
			preparedStatement.close();
			connection.close();
			return alts.values();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void addBan(Ban ban) {
		deleteBan(ban);
		try {
			Connection connection = DatabaseConnectionPool.instance.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO bans (reason, admin, player, type, time) VALUES (?, ?, ?, ?, ?)");
			preparedStatement.setString(1, ban.getReason());
			preparedStatement.setInt(2, ban.getAdminID());
			preparedStatement.setInt(3, ban.getPlayerID());
			preparedStatement.setString(4, ban.getType());
			preparedStatement.setDate(5, ban.getTime());
			preparedStatement.execute();
			preparedStatement.close();
			connection.close();

			playerBans.put(ban.getPlayerID(), new SoftReference<>(ban));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteBan(Ban ban) {
		try {
			Connection connection = DatabaseConnectionPool.instance.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM bans WHERE player = ?");
			preparedStatement.setInt(1, ban.getPlayerID());
			preparedStatement.execute();
			preparedStatement.close();
			connection.close();

			playerBans.remove(ban.getPlayerID());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected static Ban getBan(String username, UUID uuid, boolean useCaches) {
		int userID = getUserID(username, uuid);
		if(userID < 1)
			return null;

		if(playerBans.containsKey(userID)) {
			SoftReference<Ban> cachedBanRef = playerBans.get(userID);
			if(cachedBanRef != null) {
				Ban cachedBan = cachedBanRef.get();
				if(useCaches && cachedBan != null && ((System.currentTimeMillis() - cachedBan.retrievalTime) < BAN_MAX_AGE_MILLIS)) {
					return cachedBan;
				} else {
					playerBans.remove(userID);
				}
			} else {
				playerBans.remove(userID);
			}
		}
		try {
			Connection connection = DatabaseConnectionPool.instance.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM bans WHERE player = ?");
			preparedStatement.setInt(1, userID);
			ResultSet resultSet = preparedStatement.executeQuery();
			Ban ret = null;
			if(resultSet.next()) {
				ret = new Ban(resultSet.getString("reason"), resultSet.getInt("admin"), resultSet.getInt("player"), resultSet.getString("type"), resultSet.getDate("time"));
				playerBans.put(userID, new SoftReference<>(ret));
			}
			preparedStatement.close();
			connection.close();
			return ret;
		} catch(Exception e) {
			e.printStackTrace();
			return new Ban("Database failure", 0, 0, "invalid", null);
		}
	}

	public static BanPlayer getUserByID(int id) {
		if(playerUUIDs.containsKey(id)) {
			return playerUUIDs.get(id);
		}
		try {
			Connection connection = DatabaseConnectionPool.instance.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT uuid, name FROM players WHERE id = ?");
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			BanPlayer ret = null;
			if(resultSet.next()) {
				UUID uuid = UUID.fromString(resultSet.getString("uuid"));
				ret = new BanPlayer(uuid, resultSet.getString("name"));
				playerIDs.put(uuid, id);
				playerUUIDs.put(id, ret);
			}
			preparedStatement.close();
			connection.close();
			return ret;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static int getUserID(String username, UUID uuid) {
		return getUserID(username, uuid, false);
	}

	public static int getUserID(String username, UUID uuid, boolean create) {
		if(username != null && username.charAt(0) == '[')
			uuid = UUID.nameUUIDFromBytes(("SPECIAL:" + username).getBytes());

		if(uuid == null)
			uuid = FishBansResolver.getUUID(username);

		if(uuid != null && playerIDs.containsKey(uuid))
			return playerIDs.get(uuid);

		try {
			Connection connection = DatabaseConnectionPool.instance.getConnection();
			PreparedStatement preparedStatement;
			if(uuid != null) {
				preparedStatement = connection.prepareStatement("SELECT id, name, uuid FROM players WHERE uuid = ?");
				preparedStatement.setString(1, uuid.toString());
			} else {
				preparedStatement = connection.prepareStatement("SELECT id, name, uuid FROM players WHERE name = ?");
				preparedStatement.setString(1, username);
			}
			ResultSet resultSet = preparedStatement.executeQuery();
			int ret = 0;
			if(resultSet.next()) {
				ret = resultSet.getInt("id");
				uuid = UUID.fromString(resultSet.getString("uuid"));
				if(username == null)
					username = resultSet.getString("name");
				if(!resultSet.getString("name").equals(username)) {
					preparedStatement.close();
					preparedStatement = connection.prepareStatement("UPDATE players SET name = ? WHERE uuid = ?");
					preparedStatement.setString(1, username);
					preparedStatement.setString(2, uuid.toString());
					preparedStatement.execute();
				}
				playerIDs.put(uuid, ret);
				playerUUIDs.put(ret, new BanPlayer(uuid, username));
			} else if(create) {
				if(uuid == null)
					throw new RuntimeException("Cannot create player without UUID");
				preparedStatement.close();
				preparedStatement = connection.prepareStatement("INSERT INTO players (name, uuid) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
				preparedStatement.setString(1, username);
				preparedStatement.setString(2, uuid.toString());
				ret = preparedStatement.executeUpdate();
				resultSet = preparedStatement.getGeneratedKeys();
				if(resultSet.next()) {
					ret = resultSet.getInt(1);
					playerIDs.put(uuid, ret);
					playerUUIDs.put(ret, new BanPlayer(uuid, username));
				}
			}
			preparedStatement.close();
			connection.close();
			return ret;
		} catch(Exception e) {
			return 0;
		}
	}
}