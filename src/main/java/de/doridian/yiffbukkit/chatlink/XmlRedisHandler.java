package de.doridian.yiffbukkit.chatlink;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XmlRedisHandler extends AbstractJedisPubSub {
	public XmlRedisHandler() {
		RedisManager.readJedisPool.getResource().subscribe(this, "yiffbukkit:from_server");
	}

	private static final String PLAYER_FORMAT = "<span onClick=\"suggest_command('/pm %1$s ')\">%2$s</span>";
	private static final String MESSAGE_FORMAT = PLAYER_FORMAT + "<color name=\"white\">: %3$s</color>";
	private static final String KICK_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">was kicked (%3$s)!</color>";
	private static final String EMOTE_FORMAT = "* " + PLAYER_FORMAT + " <color name=\"gray\">%3$s</color>";
	private static final String QUIT_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">disconnected!</color>";
	private static final String JOIN_FORMAT = "<color name=\"dark_green\">[+]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">joined!</color>";

	@Override
	public void onMessage(final String channel, final String c_message) {
		try {
			final String[] split = c_message.split("\\|", 3);

			//SERVER|USER|MESSAGE
			final String server = split[0];
			final String ply = split[1];
			final String message = split[2];

			final String[] params = formatMessage(ply, message);

			// SERVER\0 USER\0 format\0 param1\0 param2
			final List<String> strings = new ArrayList<>(Arrays.asList(server, ply.toLowerCase()));
			strings.addAll(Arrays.asList(params));

			final Jedis jedis = RedisManager.readJedisPool.getResource();
			jedis.publish("yiffbukkit:to_server_xml", serialize(strings));
			RedisManager.readJedisPool.returnResource(jedis);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[] formatMessage(String ply, String message) {
		final String formattedName = PlayerHelper.getFullPlayerName(ply);

		switch (message) {
			case "\u0123join":
				PlayerHelper.addCaseCorrect(ply);

				return new String[] {
						JOIN_FORMAT,
						ply, formattedName
				};

			case "\u0123quit":
				return new String[] {
						QUIT_FORMAT,
						ply, formattedName
				};

			default:
				if (message.startsWith("/me ")) {
					final String param = message.substring(4);

					return new String[] {
							EMOTE_FORMAT,
							ply, formattedName, param
					};
				}
				else if (message.startsWith("\u0123kick ")) {
					final String param = message.substring(6);

					return new String[] {
							KICK_FORMAT,
							ply, formattedName, param
					};
				}
				else {
					return new String[] {
							MESSAGE_FORMAT,
							ply, formattedName, message
					};
				}
		}
	}

	private String serialize(List<String> strings) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String string : strings) {
			if (!first) {
				sb.append('\0');
			}
			first = false;
			sb.append(string);
		}
		return sb.toString();
	}
}
