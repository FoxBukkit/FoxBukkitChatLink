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
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.events.SlackReplyEvent;
import com.ullink.slack.simpleslackapi.impl.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
		if(event.getChannel().isDirect()) {
			handleDirectMessage(event, session);
			return;
		}

		final String channelName = event.getChannel().getName();
		if(!channelName.equalsIgnoreCase("#minecraft") && !channelName.equalsIgnoreCase("#minecraft-ops"))
			return;

		Player minecraftPlayer = lookupMinecraftAssociation(event.getSender().getId());
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

			SlackMessageHandle handle = session.sendMessage(session.findChannelByName(channel), cleanText, null, slackChatConfiguration);
			handle.waitForReply(10, TimeUnit.SECONDS);
			SlackReplyEvent slackReply = handle.getSlackReply();
			if(!slackReply.isOk()) {
				throw new IllegalStateException("Got non-ok reply from Slack: " + slackReply.toString());
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void beginLink(String username, UserInfo minecraftUser) throws CommandException {
		throw new CommandException("This command has been temporarily disabled.");

		/*final SlackUser slackUser = session.findUserByUserName(username);
		if(slackUser == null)
			throw new CommandException("The given Slack user does not exist.");

		pendingSlackLinks.put(slackUser.getId(), minecraftUser.uuid.toString());

		SlackMessageHandle handle = session.sendMessageOverWebSocket(new SlackDMChannel(slackUser), minecraftUser.name + " has requested that you link your Slack account to your Minecraft account.\nIf this is you, please respond with `link " + minecraftUser.name + "`.\nIf this is not you, it is safe to ignore this message.", null);
		handle.waitForReply(10, TimeUnit.SECONDS);
		SlackReplyEvent slackReply = handle.getSlackReply();
		if(!slackReply.isOk()) {
			throw new IllegalStateException("Got non-ok reply from Slack: " + slackReply.toString());
		}*/
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
		if(!oldSlackID.equals("")) {
			slackToMinecraftLinks.remove(oldSlackID);
		}

		minecraftToSlackLinks.put(minecraftID.toString(), slackID);
		slackToMinecraftLinks.put(slackID, minecraftID.toString());
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
			minecraftID = UUID.fromString(pendingSlackLinks.get(event.getSender().getId()));
		} catch(IllegalArgumentException e) {
			session.sendMessageOverWebSocket(event.getChannel(), "You have no pending link with that Minecraft account.", null);
			return;
		}

		final Player minecraftPlayer = new Player(minecraftID);
		if(!minecraftPlayer.getName().equalsIgnoreCase(requestedMinecraftName)) {
			session.sendMessageOverWebSocket(event.getChannel(), "You have no pending link with that Minecraft account.", null);
			return;
		}

		pendingSlackLinks.remove(event.getSender().getId());
		setMinecraftAssociation(event.getSender().getId(), minecraftPlayer.getUniqueId());
	}

	private class SlackDMChannel implements SlackChannel {
		private SlackUser user;

		public SlackDMChannel(SlackUser user) {
			this.user = user;
		}

		@Override
		public String getId() {
			final String id = "@" + user.getUserName();
			System.out.println("getId: " + id);
			return id;
		}

		@Override
		public String getName() {
			return user.getRealName();
		}

		@Override
		public Collection<SlackUser> getMembers() {
			return null;
		}

		@Override
		public String getTopic() {
			return null;
		}

		@Override
		public String getPurpose() {
			return null;
		}

		@Override
		public boolean isDirect() {
			return true;
		}
	}
}
