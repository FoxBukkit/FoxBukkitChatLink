package de.doridian.yiffbukkit.chatlink;

import de.doridian.dependencies.redis.RedisManager;

import java.util.Map;
import java.util.UUID;

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
}
