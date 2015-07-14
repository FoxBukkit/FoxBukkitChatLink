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

import com.foxelbox.dependencies.config.Configuration;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.UserInfo;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

public class SlackHandler implements SlackMessagePostedListener {
	private static Map<String, String> slackToMinecraftLinks = Main.redisManager.createCachedRedisMap("slacklinks:slack-to-mc");
	private static Map<String, String> minecraftToSlackLinks = Main.redisManager.createCachedRedisMap("slacklinks:mc-to-slack");
	private static Map<String, String> pendingSlackLinks = Main.redisManager.createCachedRedisMap("slacklinks:pending"); // TODO: Entries in this should expire
	private SlackSession session;

	public SlackHandler(Configuration configuration) throws IllegalArgumentException, IOException {
		String slackToken = configuration.getValue("slack-token", "");
		if(slackToken == "")
			throw new IllegalArgumentException("configuration: slack-token undefined");
		session = SlackSessionFactory.createWebSocketSlackSession(slackToken);

		session.addMessagePostedListener(this);

		session.connect();
	}

	@Override
	public void onEvent(SlackMessagePosted event, SlackSession session) {
		System.out.println("Received message from  (" + event.getSender().getId() + ") " + event.getSender().getUserName() + " to (" + event.getChannel().getId() + ") " + event.getChannel().getName());
		if(event.getChannel().isDirect()) {
			handleDirectMessage(event, session);
			return;
		}

		final String channelName = event.getChannel().getName();
		if(!channelName.equalsIgnoreCase("#minecraft") && !channelName.equalsIgnoreCase("#minecraft-ops"))
			return;

		Player minecraftPlayer = lookupMinecraftAssociation(event.getSender().getUserName());
		if(minecraftPlayer == null)
			return;

		ChatMessageIn messageIn = new ChatMessageIn();

		messageIn.type = "text";
		messageIn.server = "Chat";
		messageIn.context = UUID.randomUUID();
		messageIn.timestamp = Math.round(new Double(event.getTimeStamp()));
		messageIn.from = new UserInfo(minecraftPlayer.getUniqueId(), minecraftPlayer.getName());

		messageIn.contents = event.getMessageContent();
		if(channelName.equalsIgnoreCase("#minecraft-ops"))
			messageIn.contents = "#" + messageIn.contents;
		else if(messageIn.contents.charAt(0) == '.')
			messageIn.contents = "/" + messageIn.contents.substring(1);

		RedisHandler.incomingMessage(messageIn);
	}

	public void sendMessage(ChatMessageOut message) {
		if(!message.type.equalsIgnoreCase("text")) { // We ignore non-text messages
			return;
		}

		try {
			SlackChatConfiguration slackChatConfiguration = SlackChatConfiguration.getConfiguration();

			if(message.from != null && message.from.name.length() > 0) {
				slackChatConfiguration.withName(message.from.name);
				slackChatConfiguration.withIcon("https://minotar.net/avatar/" + URLEncoder.encode(message.from.name, "UTF-8") + "/48.png");
			} else {
				slackChatConfiguration.asUser();
			}

			final String channel;
			switch(message.to.type) {
				case "all":
					channel = "minecraft";
					break;
				case "permission":
					if(message.to.filter.length == 1 && message.to.filter[0].equals("foxbukkit.opchat")) {
						// op chat
						channel = "minecraft-ops";
						break;
					}
					return;
				default:
					// We presently can't handle PMs.
					return;
			}

			final String cleanText = message.contents.replaceAll("<[^>]+>", "").replaceAll("&apos;", "'").replaceAll("&quot;", "\""); // Remove all of the HTML tags and fix &apos; and &quot;

			session.sendMessage(session.findChannelByName(channel), cleanText, null, slackChatConfiguration);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void beginLink(String username, UserInfo minecraftUser) throws CommandException {
		SlackUser slackUser = session.findUserByUserName(username);
		if(slackUser == null)
			throw new CommandException("The given Slack user does not exist.");

		pendingSlackLinks.put(slackUser.getUserName(), minecraftUser.uuid.toString());

		String channelID = "D" + slackUser.getId().substring(1); // DM channel ID is the same as user ID with the U replaced with D.

		session.sendMessageOverWebSocket(session.findChannelById(channelID), minecraftUser.name + " has requested that you link your Slack account to your Minecraft account.\nIf this is you, please respond with `link " + minecraftUser.name + "`.\nIf this is not you, it is safe to ignore this message.", null);
	}

	private Player lookupMinecraftAssociation(String username) {
		final UUID minecraftID;
		try {
			minecraftID = UUID.fromString(slackToMinecraftLinks.get(username));
		} catch(IllegalArgumentException e) {
			return null;
		}

		return new Player(minecraftID);
	}

	private void setMinecraftAssociation(String slackUsername, UUID minecraftID) {
		String oldSlackUsername = minecraftToSlackLinks.get(minecraftID.toString());
		if(!oldSlackUsername.equals("")) {
			slackToMinecraftLinks.remove(oldSlackUsername);
		}

		minecraftToSlackLinks.put(minecraftID.toString(), slackUsername);
		slackToMinecraftLinks.put(slackUsername, minecraftID.toString());
	}

	private void handleDirectMessage(SlackMessagePosted event, SlackSession session) {
		if(!event.getMessageContent().toLowerCase().startsWith("link "))
			return; // We only care about account linking in DMs

		String requestedMinecraftName = event.getMessageContent().substring(5).trim();
		if(requestedMinecraftName.equals("")) { // The name that they gave us was empty.
			session.sendMessageOverWebSocket(event.getChannel(), "You must provide a Minecraft name for the `link` command.\nFor example: `link MinecraftName`", null);
			return;
		}

		final UUID minecraftID;
		try {
			minecraftID = UUID.fromString(pendingSlackLinks.get(event.getSender().getUserName()));
		} catch(IllegalArgumentException e) {
			session.sendMessageOverWebSocket(event.getChannel(), "You have no pending link with that Minecraft account.", null);
			return;
		}

		final Player minecraftPlayer = new Player(minecraftID);
		if(!minecraftPlayer.getName().equalsIgnoreCase(requestedMinecraftName)) {
			session.sendMessageOverWebSocket(event.getChannel(), "You have no pending link with that Minecraft account.", null);
			return;
		}

		pendingSlackLinks.remove(event.getSender().getUserName());
		setMinecraftAssociation(event.getSender().getUserName(), minecraftPlayer.getUniqueId());
	}

	/*private static void publishToSlack(ChatMessageOut message) {
		if(!message.type.equalsIgnoreCase("text")) { // We ignore non-text messages
			return;
		}

		String username;
		if(message.from != null && message.from.name.length() > 0) {
			username = message.from.name;
		} else {
			username = "(no username)";
		}

		String channel;
		switch(message.to.type) {
			case "all":
				channel = "#minecraft";
				break;
			case "permission":
				if(message.to.filter.length == 1 && message.to.filter[0].equals("foxbukkit.opchat")) {
					// op chat
					channel = "#minecraft-ops";
					break;
				}
				return;
			default:
				return;
		}

		// TODO: Do something better than this simple replace.
		final String cleanText = message.contents.replaceAll("<[^>]+>", "").replaceAll("&apos;", "'"); // Remove all of the HTML tags and fix &apos;

		try {
			final URL slackPostURL = new URL("https://slack.com/api/chat.postMessage");

			final Map<String, Object> params = new LinkedHashMap<>();
			params.put("token", Main.configuration.getValue("slack-token", ""));
			params.put("channel", channel);
			params.put("username", username);
			if(message.from != null && message.from.name.length() > 0) {
				// Only set the avatar URL if there's a name attached to this message.
				params.put("icon_url", "https://minotar.net/avatar/" + URLEncoder.encode(message.from.name, "UTF-8") + "/48.png");
			}

			params.put("parse", "none"); // Slack shouldn't do any modification to our text.
			params.put("link_names", "false"); // Slack shouldn't try to link names.
			params.put("unfurl_media", "false"); // Please don't unfurl media from untrusted users.
			params.put("text", cleanText);

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
	}*/
}
