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
import com.foxelbox.foxbukkit.chatlink.util.SlackException;
import com.google.common.io.CharStreams;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.SlackReplyEvent;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SlackHandler implements SlackMessagePostedListener {
	final private static Map<String, String> slackToMinecraftLinks = Main.redisManager.createCachedRedisMap("slacklinks:slack-to-mc");
	final private static Map<String, String> minecraftToSlackLinks = Main.redisManager.createCachedRedisMap("slacklinks:mc-to-slack");
	final private static Map<String, String> pendingSlackLinks = Main.redisManager.createCachedRedisMap("slacklinks:pending"); // TODO: Entries in this should expire

	final private SlackSession session;
	final private String slackAuthToken;

	public SlackHandler(Configuration configuration) throws IllegalArgumentException, IOException {
		slackAuthToken = configuration.getValue("slack-token", "");
		if(slackAuthToken.equals(""))
			throw new IllegalArgumentException("configuration: slack-token undefined");
		session = SlackSessionFactory.createWebSocketSlackSession(slackAuthToken);

		session.addMessagePostedListener(this);

		session.connect();
	}

	@Override
	public void onEvent(SlackMessagePosted event, SlackSession session) {
		try {
			final SlackUser sender = event.getSender();
			if(sender == null || sender.getId() == session.sessionPersona().getId())
				return; // Ignore our own messages.

			if(event.getChannel().isDirect()) {
				handleDirectMessage(event, session);
				return;
			}

			final String channelName = event.getChannel().getName();
			if(!channelName.equalsIgnoreCase("minecraft") && !channelName.equalsIgnoreCase("minecraft-ops"))
				return;

			Player minecraftPlayer = lookupMinecraftAssociation(event.getSender().getId());
			if(minecraftPlayer == null)
				return;

			ChatMessageIn messageIn = new ChatMessageIn();

			messageIn.type = "text";
			messageIn.server = "Slack";
			messageIn.context = UUID.randomUUID();
			messageIn.timestamp = Math.round(new Double(event.getTimeStamp()));
			messageIn.from = new UserInfo(minecraftPlayer.getUniqueId(), minecraftPlayer.getName());

			messageIn.contents = event.getMessageContent();
			if(channelName.equalsIgnoreCase("minecraft-ops"))
				messageIn.contents = "#" + messageIn.contents;
			else if(messageIn.contents.charAt(0) == '.')
				messageIn.contents = "/" + messageIn.contents.substring(1);

			RedisHandler.incomingMessage(messageIn);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void sendMessage(ChatMessageOut message) {
		if(!message.type.equalsIgnoreCase("text")) { // We ignore non-text messages
			return;
		}

		try {
			final String channel;
			switch(message.to.type) {
				case "all":
					if(message.server.equals("Slack"))
						return; // Don't relay messages that were just sent.
					channel = "#minecraft";
					break;
				case "permission":
					if(message.to.filter.length == 1 && message.to.filter[0].equals("foxbukkit.opchat")) {
						if(message.server.equals("Slack"))
							return; // Don't relay messages that were just sent.
						// op chat
						channel = "#minecraft-ops";
						break;
					}
					return;
				case "player":
					this.sendPlayerMessage(message);
					return;
				default:
					return;
			}

			final String cleanText = cleanMessageContents(message);

			final Player as;
			if(message.from != null)
				as = new Player(message.from.uuid);
			else
				as = null;

			sendToSlack(channel, cleanText, as);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void sendPlayerMessage(ChatMessageOut message) throws IOException {
		final String cleanText = cleanMessageContents(message);

		final Player as;
		if(message.from != null)
			as = new Player(message.from.uuid);
		else
			as = null;

		for(String toID : message.to.filter) {
			String slackID = minecraftToSlackLinks.get(toID);
			if(slackID == null) // No Slack account is associated with this ID
				continue;

			SlackUser user = session.findUserById(slackID);
			if(user == null || user.isDeleted()) // This slack user no longer exists
				continue;

			sendToSlack(getDMID(user), cleanText, as);
		}
	}

	private String cleanMessageContents(ChatMessageOut message) {
		String cleanText = message.contents.replaceAll("<[^>]+>", "").replaceAll("&apos;", "'").replaceAll("&quot;", "\""); // Remove all of the HTML tags and fix &apos; and &quot;
		if(message.server != null && message.server != "Slack")
			cleanText = "[" + message.server + "] " + cleanText;

		return cleanText;
	}

	public void beginLink(String username, UserInfo minecraftUser) throws CommandException, IOException {
		final SlackUser slackUser = session.findUserByUserName(username);
		if(slackUser == null || slackUser.isDeleted())
			throw new CommandException("The given Slack user does not exist.");

		pendingSlackLinks.put(slackUser.getId(), minecraftUser.uuid.toString());
		sendToSlack(getDMID(slackUser), minecraftUser.name + " has requested that you link your Slack account to your Minecraft account.\nIf this is you, please respond with `link " + minecraftUser.name + "`.\nIf this is not you, it is safe to ignore this message.");
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

	private void setMinecraftAssociation(String slackID, UUID minecraftID) {
		String oldSlackID = minecraftToSlackLinks.get(minecraftID.toString());
		if(oldSlackID != null) {
			slackToMinecraftLinks.remove(oldSlackID);
		}

		minecraftToSlackLinks.put(minecraftID.toString(), slackID);
		slackToMinecraftLinks.put(slackID, minecraftID.toString());
	}

	private void handleDirectMessage(SlackMessagePosted event, SlackSession session) throws IOException {
		if(!event.getMessageContent().toLowerCase().startsWith("link"))
			return; // We only care about account linking in DMs

		String requestedMinecraftName = event.getMessageContent().substring(4).trim();
		if(requestedMinecraftName.equals("")) { // The name that they gave us was empty.
			sendToSlack(event.getChannel().getId(), "You must provide a Minecraft name for the `link` command.\nFor example: `link MinecraftName`");
			return;
		}

		final UUID minecraftID;
		try {
			minecraftID = UUID.fromString(pendingSlackLinks.get(event.getSender().getId()));
		} catch(NullPointerException e) {
			sendToSlack(event.getChannel().getId(), "That account has not requested to be linked with your Slack account from Minecraft.");
			return;
		} catch(IllegalArgumentException e) {
			sendToSlack(event.getChannel().getId(), "That account has not requested to be linked with your Slack account from Minecraft.");
			return;
		}

		final Player minecraftPlayer = new Player(minecraftID);
		if(!minecraftPlayer.getName().equalsIgnoreCase(requestedMinecraftName)) {
			sendToSlack(event.getChannel().getId(), "That account has not requested to be linked with your Slack account from Minecraft.");
			return;
		}

		pendingSlackLinks.remove(event.getSender().getId());
		setMinecraftAssociation(event.getSender().getId(), minecraftPlayer.getUniqueId());

		sendToSlack(event.getChannel().getId(), "Successfully linked your Minecraft account.");
	}

	private void sendToSlack(String channelID, String message, Player asPlayer) throws IOException {
		SlackChatConfiguration slackChatConfiguration = SlackChatConfiguration.getConfiguration();

		if(asPlayer != null && asPlayer.getName().length() > 0) {
			slackChatConfiguration.withName(asPlayer.getName());
			try {
				slackChatConfiguration.withIcon("https://minotar.net/avatar/" + URLEncoder.encode(asPlayer.getName(), "UTF-8") + "/48.png");
			} catch(UnsupportedEncodingException e) {
				// This should never happen.
				e.printStackTrace();
			}
		} else {
			slackChatConfiguration.asUser();
		}

		SlackMessageHandle handle = session.sendMessage(new SlackRawChannel(channelID), message, null, slackChatConfiguration);
		handle.waitForReply(1, TimeUnit.SECONDS);
		SlackReplyEvent slackReply = handle.getSlackReply();
		if(slackReply == null) {
			throw new IOException("Got no reply from Slack API after 1 second");
		} else if(!slackReply.isOk()) {
			throw new IOException("Got non-ok reply from Slack");
		}
	}

	private void sendToSlack(String channelID, String message) throws IOException {
		this.sendToSlack(channelID, message, null);
	}

	private String getDMID(SlackUser user) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost("https://slack.com/api/im.open");
		List<NameValuePair> nameValuePairList = new ArrayList<>();
		nameValuePairList.add(new BasicNameValuePair("token", slackAuthToken));
		nameValuePairList.add(new BasicNameValuePair("user", user.getId()));
		try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairList, "UTF-8"));
			HttpResponse response = client.execute(request);
			String jsonResponse = CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
			JSONObject resultObject = parseObject(jsonResponse);

			boolean ok = (boolean) resultObject.get("ok");
			if(!ok) {
				throw new SlackException((String) resultObject.get("error"));
			}

			JSONObject channelObject = (JSONObject) resultObject.get("channel");
			String channelID = (String) channelObject.get("id");

			return channelID;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject parseObject(String json) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject object = (JSONObject) parser.parse(json);
			return object;
		} catch(ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private class SlackRawChannel implements SlackChannel {
		final private String id;
		final private SlackChannel underlyingChannel;

		public SlackRawChannel(String id) {
			this.id = id;
			this.underlyingChannel = session.findChannelById(id);
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			if(this.underlyingChannel == null)
				return null;

			return this.underlyingChannel.getName();
		}

		@Override
		public Collection<SlackUser> getMembers() {
			if(this.underlyingChannel == null)
				return null;

			return this.underlyingChannel.getMembers();
		}

		@Override
		public String getTopic() {
			if(this.underlyingChannel == null)
				return null;

			return this.underlyingChannel.getTopic();
		}

		@Override
		public String getPurpose() {
			if(this.underlyingChannel == null)
				return null;

			return this.underlyingChannel.getPurpose();
		}

		@Override
		public boolean isDirect() {
			if(this.underlyingChannel == null) {
				return this.id.charAt(0) == '@' || this.id.charAt(0) == 'D';
			}

			return this.underlyingChannel.isDirect();
		}
	}
}
