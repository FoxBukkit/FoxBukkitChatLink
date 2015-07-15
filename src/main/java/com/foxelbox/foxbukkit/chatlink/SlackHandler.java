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
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.util.regex.Pattern;

public class SlackHandler implements SlackMessagePostedListener {
	final private static Pattern htmlTags = Pattern.compile("<[^>]+>");

	final private static Map<String, String> slackToMinecraftLinks = Main.redisManager.createCachedRedisMap("slacklinks:slack-to-mc");
	final private static Map<String, String> minecraftToSlackLinks = Main.redisManager.createCachedRedisMap("slacklinks:mc-to-slack");
	final private static Map<String, String> pendingSlackLinks = Main.redisManager.createCachedRedisMap("slacklinks:pending"); // TODO: Entries in this should expire

	final private Map<String, String> contextResponses = new HashMap<>();
	final private Map<String, StringBuilder> contextBuffers = new HashMap<>();

	final private SlackSession session;
	final private String slackAuthToken;

	public SlackHandler(Configuration configuration) throws IllegalArgumentException, IOException {
		slackAuthToken = configuration.getValue("slack-token", "");
		if(slackAuthToken.equals("")) {
			System.err.println("configuration: slack-token undefined. SLACK DISABLED");
			session = null;
			return;
		}

		session = SlackSessionFactory.createWebSocketSlackSession(slackAuthToken);

		session.addMessagePostedListener(this);

		session.connect();
	}

	@Override
	public void onEvent(SlackMessagePosted event, SlackSession session) {
		try {
			final SlackUser sender = event.getSender();
			if(sender == null || sender.getId().equals(session.sessionPersona().getId()))
				return; // Ignore our own messages.

			if(event.getChannel().isDirect()) {
				handleDirectMessage(event);
				return;
			}

			final String channelName = event.getChannel().getName();
			if(!channelName.equalsIgnoreCase("minecraft") && !channelName.equalsIgnoreCase("minecraft-ops"))
				return;

			handleSlackMessage(event);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void handleSlackMessage(SlackMessagePosted event) {
		Player minecraftPlayer = lookupMinecraftAssociation(event.getSender().getId());
		if(minecraftPlayer == null)
			return;

		ChatMessageIn messageIn = new ChatMessageIn();

		messageIn.type = "text";
		messageIn.server = "Slack";

		messageIn.context = UUID.randomUUID();
		contextResponses.put(messageIn.context.toString(), event.getChannel().getId());

		messageIn.timestamp = Math.round(new Double(event.getTimeStamp()));
		messageIn.from = new UserInfo(minecraftPlayer.getUniqueId(), minecraftPlayer.getName());

		messageIn.contents = StringEscapeUtils.unescapeHtml4(event.getMessageContent());

		if(messageIn.contents.charAt(0) == '.')
			messageIn.contents = "/" + messageIn.contents.substring(1);
		else if(event.getChannel().getName().equalsIgnoreCase("minecraft-ops"))
			messageIn.contents = "#" + messageIn.contents;

		ChatQueueHandler.incomingMessage(messageIn);
	}

	public void sendMessage(ChatMessageOut message) {
		if(session == null) {
			return;
		}

		if(!message.type.equalsIgnoreCase("text")) { // We ignore non-text messages
			if(message.finalizeContext) {
				contextResponses.remove(message.context.toString());
				contextBuffers.remove(message.context.toString());
			}
			return;
		}

		StringBuilder contextBuffer = contextBuffers.get(message.context.toString());
		if(!message.finalizeContext) {
			// Buffer this so that we can make fewer calls to Slack

			if(contextBuffer == null) {
				contextBuffer = new StringBuilder();
				contextBuffers.put(message.context.toString(), contextBuffer);
			}

			if(contextBuffer.length() > 0)
				contextBuffer.append('\n');

			contextBuffer.append(cleanMessageContents(message));
			return;
		}

		try {
			final Collection<SlackChannel> sendTo = determineResponseChannels(message);
			if(sendTo == null)
				return; // We shouldn't send this to Slack

			final String finalMessage;
			if(contextBuffer == null)
				finalMessage = cleanMessageContents(message);
			else {
				if(contextBuffer.length() > 0)
					contextBuffer.append('\n');
				contextBuffer.append(cleanMessageContents(message));
				finalMessage = contextBuffer.toString();
			}

			final Player as;
			if(message.from != null)
				as = new Player(message.from.uuid);
			else
				as = null;

			for(final SlackChannel channel : sendTo)
				sendToSlack(channel, finalMessage, as);
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			contextResponses.remove(message.context.toString());
			contextBuffers.remove(message.context.toString());
		}
	}

	private Collection<SlackChannel> determineResponseChannels(ChatMessageOut message) {
		String responseChannel = contextResponses.get(message.context.toString());
		if(responseChannel != null) {
			return Collections.singletonList(session.findChannelById(responseChannel));
		}

		if(message.to.type.equals("all"))
			return Collections.singletonList(session.findChannelByName("minecraft"));

		if(message.to.type.equals("permission") && message.to.filter.length == 1 && message.to.filter[0].equals("foxbukkit.opchat"))
			return Collections.singletonList(session.findChannelByName("minecraft-ops"));

		return null;
	}

	private String cleanMessageContents(ChatMessageOut message) {
		String cleanText = StringEscapeUtils.unescapeHtml4(htmlTags.matcher(message.contents).replaceAll("")).replaceAll("&apos;", "'").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // Remove all of the HTML tags and fix &apos; and &quot;
		if(message.server != null && !message.server.equals("Slack"))
			cleanText = "[" + message.server + "] " + cleanText;

		return cleanText;
	}

	public void beginLink(String username, UserInfo minecraftUser) throws CommandException, IOException {
		if(session == null) {
			return;
		}

		final SlackUser slackUser = session.findUserByUserName(username);
		if(slackUser == null || slackUser.isDeleted())
			throw new CommandException("The given Slack user does not exist.");

		pendingSlackLinks.put(slackUser.getId(), minecraftUser.uuid.toString());
		sendToSlack(getDMChannel(slackUser), minecraftUser.name + " has requested that you link your Slack account to your Minecraft account.\nIf this is you, please respond with `link " + minecraftUser.name + "`.\nIf this is not you, it is safe to ignore this message.");
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

	private void handleDirectMessage(SlackMessagePosted event) throws IOException {
		final String lowerCaseMessage = event.getMessageContent().toLowerCase();
		if(lowerCaseMessage.startsWith("link")) {
			handleLinkMessage(event);
			return; // It's a linking request. Don't worry about anything else.
		}

		if(!lowerCaseMessage.startsWith("."))
			return; // Is not a command. We only accept commands in direct messages.

		handleSlackMessage(event);
	}

	private void handleLinkMessage(SlackMessagePosted event) throws IOException {
		String requestedMinecraftName = event.getMessageContent().substring(4).trim();
		if(requestedMinecraftName.equals("")) { // The name that they gave us was empty.
			sendToSlack(event.getChannel(), "You must provide a Minecraft name for the `link` command.\nFor example: `link MinecraftName`");
			return;
		}

		final UUID minecraftID;
		try {
			minecraftID = UUID.fromString(pendingSlackLinks.get(event.getSender().getId()));
		} catch(NullPointerException | IllegalArgumentException e) {
			sendToSlack(event.getChannel(), "That account has not requested to be linked with your Slack account from Minecraft.");
			return;
		}

		final Player minecraftPlayer = new Player(minecraftID);
		if(!minecraftPlayer.getName().equalsIgnoreCase(requestedMinecraftName)) {
			sendToSlack(event.getChannel(), "That account has not requested to be linked with your Slack account from Minecraft.");
			return;
		}

		pendingSlackLinks.remove(event.getSender().getId());
		setMinecraftAssociation(event.getSender().getId(), minecraftPlayer.getUniqueId());

		sendToSlack(event.getChannel(), "Successfully linked your Minecraft account.");
	}

	private void sendToSlack(SlackChannel channel, String message, Player asPlayer) throws IOException {
		if(asPlayer == null || asPlayer.getName().equals("")) {
			final SlackMessageHandle handle = session.sendMessageOverWebSocket(channel, message, null);
			handle.waitForReply(1, TimeUnit.SECONDS);
			SlackReplyEvent slackReply = handle.getSlackReply();
			if(slackReply == null) {
				throw new IOException("Got no reply from Slack API after 1 second");
			} else if(!slackReply.isOk()) {
				throw new IOException("Got non-ok reply from Slack");
			}

			return;
		}

		SlackChatConfiguration slackChatConfiguration = SlackChatConfiguration.getConfiguration();

		slackChatConfiguration.withName(asPlayer.getName());
		try {
			slackChatConfiguration.withIcon("https://minotar.net/avatar/" + URLEncoder.encode(asPlayer.getName(), "UTF-8") + "/48.png");
		} catch(UnsupportedEncodingException e) {
			// This should never happen.
			e.printStackTrace();
		}

		SlackMessageHandle handle = session.sendMessage(channel, message, null, slackChatConfiguration);
		handle.waitForReply(1, TimeUnit.SECONDS);
		SlackReplyEvent slackReply = handle.getSlackReply();
		if(slackReply == null) {
			throw new IOException("Got no reply from Slack API after 1 second");
		} else if(!slackReply.isOk()) {
			throw new IOException("Got non-ok reply from Slack");
		}
	}

	private void sendToSlack(SlackChannel channel, String message) throws IOException {
		this.sendToSlack(channel, message, null);
	}

	private SlackChannel getDMChannel(SlackUser user) {
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

			return session.findChannelById(channelID);
		} catch(IOException | ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject parseObject(String json) throws ParseException {
		JSONParser parser = new JSONParser();
		return (JSONObject) parser.parse(json);
	}
}
