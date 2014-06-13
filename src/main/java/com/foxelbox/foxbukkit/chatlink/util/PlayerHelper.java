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
package com.foxelbox.foxbukkit.chatlink.util;

import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.permissions.FoxBukkitPermissionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerHelper {
	public static Map<String,String> rankTags = Main.redisManager.createCachedRedisMap("ranktags");
    public static Map<String,String> playerTags = Main.redisManager.createCachedRedisMap("playerTags");
    public static Map<String,String> playerRankTags = Main.redisManager.createCachedRedisMap("playerRankTags");

    public static Map<String,String> rankLevels = Main.redisManager.createCachedRedisMap("ranklevels");

    public static int getRankLevel(String rank) {
        return Integer.parseInt(rankLevels.get(rank));
    }

    public static int getPlayerLevel(UUID uuid) {
        return getRankLevel(getPlayerRank(uuid));
    }

    public static String getPlayerRankTagRaw(UUID uuid) {
        final String rank = getPlayerRank(uuid).toLowerCase();
        if (rankTags.containsKey(rank))
            return rankTags.get(rank);
        return "\u00a77";
    }

	public static String getPlayerRankTag(UUID uuid) {
		final String rank = getPlayerRank(uuid).toLowerCase();
		if (playerRankTags.containsKey(uuid.toString()))
			return playerRankTags.get(uuid.toString());

		return getPlayerRankTagRaw(uuid);
	}

	public static String getPlayerTag(UUID uuid) {
		final String rankTag = getPlayerRankTag(uuid);

		if (playerTags.containsKey(uuid.toString()))
			return playerTags.get(uuid.toString()) + " " + rankTag;

		return rankTag;
	}

    public static String getPlayerTagRaw(UUID uuid, boolean rankTag) {
        final Map<String, String> tags = rankTag ? playerRankTags : playerTags;
        return tags.get(uuid.toString());
    }

    public static void setPlayerTag(UUID uuid, String tag, boolean rankTag) {
        final Map<String, String> tags = rankTag ? playerRankTags : playerTags;
        if (tag == null)
            tags.remove(uuid.toString());
        else
            tags.put(uuid.toString(), tag);
    }

    public static Map<String,String> playernicks = Main.redisManager.createCachedRedisMap("playernicks");
	public static String getPlayerNick(UUID uuid) {
		if(playernicks.containsKey(uuid.toString()))
			return playernicks.get(uuid.toString());
		else
			return null;
	}

    public static void setPlayerNick(UUID uuid, String nick) {
        if(nick == null)
            playernicks.remove(uuid.toString());
        else
            playernicks.put(uuid.toString(), nick);
    }

	public static String getPlayerRank(UUID uuid) {
		return FoxBukkitPermissionHandler.instance.getGroup(uuid);
	}

	public static String getFullPlayerName(UUID plyU, String plyN) {
		String nick = getPlayerNick(plyU);
		if(nick == null)
			nick = plyN;
		return getPlayerTag(plyU) + nick;
	}

    public static Map<String,String> playerNameToUUID = Main.redisManager.createCachedRedisMap("playerNameToUUID");
    public static Map<String,String> playerUUIDToName = Main.redisManager.createCachedRedisMap("playerUUIDToName");

    public static Player literalMatch(String name) {
        return new Player(UUID.fromString(playerNameToUUID.get(name)),name);
    }

    private static final Pattern quotePattern = Pattern.compile("^\"(.*)\"$");
    public static Player matchPlayerSingle(String subString, boolean implicitlyLiteral) throws PlayerNotFoundException, MultiplePlayersFoundException {
        if (implicitlyLiteral)
            return literalMatch(subString);

        final Matcher matcher = quotePattern.matcher(subString);

        if (matcher.matches())
            return literalMatch(matcher.group(1));

        final List<Player> players = matchPlayer(subString);

        final int c = players.size();
        if (c < 1)
            throw new PlayerNotFoundException();

        if (c > 1)
            throw new MultiplePlayersFoundException(players);

        return players.get(0);
    }

    public static List<Player> getOnlinePlayersOnServer(String name) {
        final List<String> onlineUUIDs = Main.redisManager.lrange("playersOnline:" + name, 0, -1);
        final List<Player> onlinePlayers = new ArrayList<>();
        if(onlineUUIDs == null)
            return onlinePlayers;
        for(String uuid : onlineUUIDs)
            onlinePlayers.add(new Player(UUID.fromString(uuid)));
        return onlinePlayers;
    }

    public static List<String> getAllServers() {
        return Main.redisManager.lrange("activeServers", 0, -1);
    }

    public static List<Player> getOnlinePlayersOnAllServers() {
        List<Player> onlinePlayers = new ArrayList<>();
        for(String name : getAllServers())
            onlinePlayers.addAll(getOnlinePlayersOnServer(name));
        return onlinePlayers;
    }

    public static List<Player> matchPlayer(String subString) {
        final String lowerCase = subString.toLowerCase();
        List<Player> players = new ArrayList<>();
        for (Player ply : getOnlinePlayersOnAllServers()) {
            if(players.contains(ply))
                continue;

            if (!ply.name.toLowerCase().contains(lowerCase) && !stripColor(ply.displayName.toLowerCase()).contains(lowerCase))
                continue;

            players.add(ply);
        }
        return players;
    }

    public static Player matchPlayerSingle(String subString) throws PlayerNotFoundException, MultiplePlayersFoundException {
        return matchPlayerSingle(subString, false);
    }

    public static String completePlayerName(String subString, boolean implicitlyLiteralNames) {
        Matcher matcher = quotePattern.matcher(subString);

        if (matcher.matches())
            return matcher.group(1);

        List<Player> otherplys = matchPlayer(subString);
        int c = otherplys.size();

        if (c == 0 && implicitlyLiteralNames)
            return subString;

        if (c == 1)
            return otherplys.get(0).name;

        return null;
    }

    public static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

    public static String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
