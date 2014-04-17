package de.doridian.yiffbukkit.chatlink;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
			final String[] split = c_message.split("\\|", 4);

			//SERVER|UUID|NAME|MESSAGE
			final String server = split[0];
			final UUID plyU = UUID.fromString(split[1]);
			final String plyN = split[2];
			final String message = split[3];

			final String[] params = formatMessage(plyU, plyN, message);

			// SERVER\0 UUID\0 NAME\0 format\0 param1\0 param2
			final List<String> strings = new ArrayList<>(Arrays.asList(server, plyU.toString(), plyN));
			strings.addAll(Arrays.asList(params));

			final Jedis jedis = RedisManager.readJedisPool.getResource();
			jedis.publish("yiffbukkit:to_server_xml", serialize(strings));
			RedisManager.readJedisPool.returnResource(jedis);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[] formatMessage(UUID plyU, String plyN, String message) {
		final String formattedName = PlayerHelper.getFullPlayerName(plyU, plyN);

		switch (message) {
			case "\u0123join":
				return new String[] {
						JOIN_FORMAT,
						plyN, formattedName
				};

			case "\u0123quit":
				return new String[] {
						QUIT_FORMAT,
						plyN, formattedName
				};

			default:
				if (message.startsWith("/me ")) {
					final String param = message.substring(4);

					return new String[] {
							EMOTE_FORMAT,
							plyN, formattedName, param
					};
				}
				else if (message.startsWith("\u0123kick ")) {
					final String param = message.substring(6);

					return new String[] {
							KICK_FORMAT,
							plyN, formattedName, param
					};
				}
				else {
					return new String[] {
							MESSAGE_FORMAT,
							plyN, formattedName, message
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
