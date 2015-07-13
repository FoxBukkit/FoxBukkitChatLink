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
package com.foxelbox.foxbukkit.chatlink;

import com.foxelbox.dependencies.redis.AbstractRedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.ConvCommand;
import com.foxelbox.foxbukkit.chatlink.commands.system.CommandSystem;
import com.foxelbox.foxbukkit.chatlink.filter.MuteList;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.SlackResponse;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RedisHandler extends AbstractRedisHandler {
	public RedisHandler() {
		super(Main.redisManager, RedisHandlerType.BOTH, "foxbukkit:from_server");
	}

	private static final Pattern REMOVE_DISALLOWED_CHARS = Pattern.compile("[\u00a7\r\n\t]");

	public static final String PLAYER_FORMAT = "<span onHover=\"show_text('%1$s')\" onClick=\"suggest_command('/pm %1$s ')\">%3$s</span>";
	public static final String MESSAGE_FORMAT = PLAYER_FORMAT + "<color name=\"white\">: %4$s</color>";
	public static final String KICK_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">was kicked (%4$s)!</color>";
	public static final String QUIT_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">disconnected!</color>";
	public static final String JOIN_FORMAT = "<color name=\"dark_green\">[+]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">joined!</color>";

	private static final Gson gson = new Gson();

	@Override
	public void onMessage(final String c_message) {
		try {
			final ChatMessageIn chatMessageIn;
			synchronized(gson) {
				chatMessageIn = gson.fromJson(c_message, ChatMessageIn.class);
			}

			ChatMessageOut message = formatMessage(chatMessageIn);

			if(message == null)
				return;

			message.finalize_context = true;

			sendMessage(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendMessage(ChatMessageOut message) {
		final String outMsg;
		synchronized(gson) {
			outMsg = gson.toJson(message);
		}
		Main.redisManager.publish("foxbukkit:to_server", outMsg);

		publishToSlack(message);
	}

	private static void publishToSlack(ChatMessageOut message) {
		if(!message.type.equalsIgnoreCase("all")) { // HOTFIX: Ignore all messages not intended for everyone
			return;
		}

		try {
			final URL slackPostURL = new URL("https://slack.com/api/chat.postMessage");

			final Map<String, Object> params = new LinkedHashMap<>();
			params.put("token", Main.configuration.getValue("slack-token", ""));
			params.put("channel", "temp-chatlink");
			params.put("username", message.from.name);
			params.put("icon_url", "https://minotar.net/avatar/" + URLEncoder.encode(message.from.name, "UTF-8") + "/48.png");
			params.put("text", message.contents);

			final byte[] encodedParams = encodeURLParams(params);

			final HttpURLConnection connection = (HttpURLConnection) slackPostURL.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(encodedParams.length));

			try(final DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
				wr.write(encodedParams);
			}

			SlackResponse slackResponse;
			try(final BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
				synchronized(gson) {
					slackResponse = gson.fromJson(inputStream, SlackResponse.class);
				}
			}

			if(!slackResponse.ok) {
				throw new Exception("Error occurred while calling Slack API: " + slackResponse.error);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static byte[] encodeURLParams(Map<String, Object> params) {
		final StringBuilder encoded = new StringBuilder();

		try {
			for(Map.Entry<String, Object> param : params.entrySet()) {
				if(encoded.length() != 0)
					encoded.append('&');
				encoded.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				encoded.append('=');
				encoded.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}

			return encoded.toString().getBytes("UTF-8");
		} catch(UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ChatMessageOut runFormatAndStore(ChatMessageIn messageIn, String format, String[] formatArgs) {
		return new ChatMessageOut(messageIn, format, formatArgs);
	}

	private static ChatMessageOut formatMessage(ChatMessageIn messageIn) {
		final String plyN = messageIn.from.name;
		final String formattedName = PlayerHelper.getFullPlayerName(messageIn.from.uuid, plyN);

		final Player sender = new Player(messageIn.from.uuid, messageIn.from.name);

		String messageStr = messageIn.contents;

		switch(messageIn.type) {
			case "playerstate":
				switch(messageStr) {
					case "join":
						return runFormatAndStore(messageIn, JOIN_FORMAT, new String[]{plyN, messageIn.from.uuid.toString(), formattedName});

					case "quit":
						return runFormatAndStore(messageIn, QUIT_FORMAT, new String[]{plyN, messageIn.from.uuid.toString(), formattedName});
				}

				if(messageStr.startsWith("kick ")) {
					final String param = messageStr.substring(5);
					if(param.startsWith("[Kicked]")) {
						sender.kick(param, true);
					} else {
						sender.showKickMessage(param, true);
					}
				}

				break;

			case "text":
				messageStr = REMOVE_DISALLOWED_CHARS.matcher(messageStr).replaceAll("");

				if(messageStr.length() > 1) {
					if(messageStr.charAt(0) == '#' && messageStr.charAt(1) == '!')
						messageStr = "/staffnotice " + messageStr.substring(2);
				}

				if(messageStr.charAt(0) == '#') {
					messageStr = "/opchat " + messageStr.substring(1);
				}

				if(messageStr.charAt(0) == '/') {
					messageStr = messageStr.substring(1).trim();
					int argPos = messageStr.indexOf(' ');
					final String argStr;
					final String commandName;
					if(argPos > 0) {
						argStr = messageStr.substring(argPos + 1);
						commandName = messageStr.substring(0, argPos);
					} else {
						argStr = "";
						commandName = messageStr;
					}
					return CommandSystem.instance.runCommand(sender, messageIn, commandName, argStr);
				} else {
					if(MuteList.isMuted(sender)) {
						return null;
					}

					if(ConvCommand.handleConvMessage(messageIn, formattedName, messageStr, false)) {
						return null;
					}

					return runFormatAndStore(messageIn, MESSAGE_FORMAT, new String[]{plyN, messageIn.from.uuid.toString(), formattedName, messageStr});
				}
		}

		throw new RuntimeException("Unprocessable message: " + messageIn.type + " => " + messageStr);
	}
}
