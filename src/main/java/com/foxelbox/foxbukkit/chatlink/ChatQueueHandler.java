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

import com.foxelbox.foxbukkit.chatlink.commands.ConvCommand;
import com.foxelbox.foxbukkit.chatlink.commands.system.CommandSystem;
import com.foxelbox.foxbukkit.chatlink.filter.MuteList;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.google.gson.Gson;
import org.zeromq.ZMQ;

import java.util.regex.Pattern;

public class ChatQueueHandler {
	public static final String PLAYER_FORMAT = "<span onHover=\"show_text('%1$s')\" onClick=\"suggest_command('/pm %1$s ')\">%3$s</span>";
	public static final String MESSAGE_FORMAT = PLAYER_FORMAT + "<color name=\"white\">: %4$s</color>";
	public static final String KICK_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">was kicked (%4$s)!</color>";
	public static final String QUIT_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">disconnected!</color>";
	public static final String JOIN_FORMAT = "<color name=\"dark_green\">[+]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">joined!</color>";
	private static final Pattern REMOVE_DISALLOWED_CHARS = Pattern.compile("[\u00a7\r\n\t]");
	private static final Gson gson = new Gson();

	private static ZMQ.Socket sender;
	private final ZMQ.Socket receiver;

	public ChatQueueHandler() {
		receiver = Main.zmqContext.socket(ZMQ.PULL);
		receiver.connect(Main.configuration.getValue("zmq-broker-to-link", "tcp://127.0.0.1:5557"));

		sender = Main.zmqContext.socket(ZMQ.PUB);
		sender.connect(Main.configuration.getValue("zmq-link-to-broker", "tcp://127.0.0.1:5558"));

		Thread t = new Thread() {
			@Override
			public void run() {
				while(!Thread.currentThread().isInterrupted()) {
					String str = receiver.recvStr(Main.CHARSET);
					onMessage(str);
				}
			}
		};
		t.setDaemon(true);
		t.setName("ZMQ REP");
		t.start();
	}

	public static void incomingMessage(final ChatMessageIn chatMessageIn) {
		final ChatMessageOut message = formatMessage(chatMessageIn);

		if(message == null)
			return;

		message.finalizeContext = true;

		sendMessage(message);
	}

	private final static byte[] CMO = new byte[] { 'C', 'M', 'O'};

	public static void sendMessage(ChatMessageOut message) {
		final String outMsg;
		synchronized(gson) {
			outMsg = gson.toJson(message);
		}
		sender.send(CMO, ZMQ.SNDMORE);
		sender.send(outMsg);

		Main.slackHandler.sendMessage(message);
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

	public void onMessage(final String c_message) {
		try {
			final ChatMessageIn chatMessageIn;
			synchronized(gson) {
				chatMessageIn = gson.fromJson(c_message, ChatMessageIn.class);
			}

			incomingMessage(chatMessageIn);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
