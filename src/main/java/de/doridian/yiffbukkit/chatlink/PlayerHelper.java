package de.doridian.yiffbukkit.chatlink;

import java.util.Map;

public class PlayerHelper {
	private static Map<String,String> rankTags = RedisManager.createKeptMap("ranktags");
	private static Map<String,String> playerTags = RedisManager.createKeptMap("playerTags");
	private static Map<String,String> playerRankTags = RedisManager.createKeptMap("playerRankTags");


	private static String getPlayerRankTag(String name) {
		name = name.toLowerCase();
		final String rank = getPlayerRank(name).toLowerCase();
		if (playerRankTags.containsKey(name))
			return playerRankTags.get(name);

		if (rankTags.containsKey(rank))
			return rankTags.get(rank);

		return "\u00a77";
	}

	public static String getPlayerTag(String name) {
		name = name.toLowerCase();
		final String rankTag = getPlayerRankTag(name);

		if (playerTags.containsKey(name))
			return playerTags.get(name) + " " + rankTag;

		return rankTag;
	}

	private static Map<String,String> caseCorrectNames = RedisManager.createKeptMap("playercasecorret");
	public static void addCaseCorrect(String name) {
		caseCorrectNames.put(name.toLowerCase(), name);
	}

	private static Map<String,String> playernicks = RedisManager.createKeptMap("playernicks");
	public static String getPlayerNick(String name) {
		name = name.toLowerCase();
		if(playernicks.containsKey(name))
			return playernicks.get(name);
		else
			return null;
	}

	private static Map<String,String> playerGroups = RedisManager.createKeptMap("playergroups");
	public static String getPlayerRank(String name) {
		final String rank = playerGroups.get(name.toLowerCase());
		if (rank == null)
			return "guest";

		return rank;
	}

	public static String getFullPlayerName(String ply) {
		String nick = getPlayerNick(ply);
		if(nick == null) {
			nick = ply;
		}
		return getPlayerTag(ply) + nick;
	}
}
