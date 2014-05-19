package de.doridian.yiffbukkit.chatlink.util;

import de.doridian.dependencies.redis.RedisManager;
import de.doridian.yiffbukkit.chatlink.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerHelper {
	private static Map<String,String> rankTags = RedisManager.createCachedRedisMap("ranktags");
	private static Map<String,String> playerTags = RedisManager.createCachedRedisMap("playerTags");
	private static Map<String,String> playerRankTags = RedisManager.createCachedRedisMap("playerRankTags");


	private static String getPlayerRankTag(UUID uuid) {
		final String rank = getPlayerRank(uuid).toLowerCase();
		if (playerRankTags.containsKey(uuid.toString()))
			return playerRankTags.get(uuid.toString());

		if (rankTags.containsKey(rank))
			return rankTags.get(rank);

		return "\u00a77";
	}

	public static String getPlayerTag(UUID uuid) {
		final String rankTag = getPlayerRankTag(uuid);

		if (playerTags.containsKey(uuid.toString()))
			return playerTags.get(uuid.toString()) + " " + rankTag;

		return rankTag;
	}

	private static Map<String,String> playernicks = RedisManager.createCachedRedisMap("playernicks");
	public static String getPlayerNick(UUID uuid) {
		if(playernicks.containsKey(uuid.toString()))
			return playernicks.get(uuid.toString());
		else
			return null;
	}

	private static Map<String,String> playerGroups = RedisManager.createCachedRedisMap("playergroups");
	public static String getPlayerRank(UUID uuid) {
		final String rank = playerGroups.get(uuid.toString());
		if (rank == null)
			return "guest";

		return rank;
	}

	public static String getFullPlayerName(UUID plyU, String plyN) {
		String nick = getPlayerNick(plyU);
		if(nick == null)
			nick = plyN;
		return getPlayerTag(plyU) + nick;
	}

    public static Map<String,String> playerNameToUUID = RedisManager.createCachedRedisMap("playerNameToUUID");
    public static Map<String,String> playerUUIDToName = RedisManager.createCachedRedisMap("playerUUIDToName");

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
        final List<String> onlineUUIDs = RedisManager.lrange("playersOnline:" + name, 0, -1);
        final List<Player> onlinePlayers = new ArrayList<>();
        if(onlineUUIDs == null)
            return onlinePlayers;
        for(String uuid : onlineUUIDs)
            onlinePlayers.add(new Player(UUID.fromString(uuid)));
        return onlinePlayers;
    }

    public static List<String> getAllServers() {
        return RedisManager.lrange("activeServers", 0, -1);
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

            if (!ply.name.contains(lowerCase) && !stripColor(ply.displayName.toLowerCase()).contains(lowerCase))
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