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

import com.foxelbox.foxbukkit.chatlink.commands.system.CommandSystem;
import com.google.gson.Gson;
import com.foxelbox.dependencies.redis.AbstractRedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.*;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessage;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.json.UserInfo;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.*;
import java.util.regex.Pattern;

public class RedisHandler extends AbstractRedisHandler {
    public RedisHandler() {
        super(Main.redisManager, "foxbukkit:from_server");
    }

    private static final Pattern REMOVE_COLOR_CODE = Pattern.compile("\u00a7.?");

	public static final String PLAYER_FORMAT = "<span onClick=\"suggest_command('/pm %1$s ')\">%2$s</span>";
    public static final String MESSAGE_FORMAT = PLAYER_FORMAT + "<color name=\"white\">: %3$s</color>";
    public static final String KICK_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">was kicked (%3$s)!</color>";
    public static final String QUIT_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">disconnected!</color>";
    public static final String JOIN_FORMAT = "<color name=\"dark_green\">[+]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">joined!</color>";

    private static final Gson gson = new Gson();

	@Override
	public void onMessage(final String c_message) {
		try {
			final String[] split = c_message.split("\\|", 4);

			//SERVER|UUID|NAME|MESSAGE
			final String server = split[0];
			final UUID plyU = UUID.fromString(split[1]);
			final String plyN = split[2];

            ChatMessage message = formatMessage(new ChatMessage(server, new UserInfo(plyU, plyN), split[3]));

            if(message == null)
                return;

            sendMessage(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void sendMessage(ChatMessage message) {
        final String outMsg;
        synchronized (gson) {
            outMsg = gson.toJson(message);
        }
        Main.redisManager.publish("foxbukkit:to_server", outMsg);
    }

    private static ChatMessage runFormatAndStore(ChatMessage message, String format, String[] formatArgs, String plain) {
        message.contents = new MessageContents(plain, format, formatArgs);
        return message;
    }

	private static ChatMessage formatMessage(ChatMessage message) {
        final String plyN = message.from.name;
		final String formattedName = PlayerHelper.getFullPlayerName(message.from.uuid, plyN);

        String messageStr = REMOVE_COLOR_CODE.matcher(message.contents.plain).replaceAll("");

        if(messageStr.charAt(0) == '#')
            messageStr = "/opchat " +  messageStr.substring(1);

		switch (messageStr) {
			case "\u0123join":
                return runFormatAndStore(message,
                        JOIN_FORMAT,
                        new String[] {
                            plyN, formattedName
                        },
                        "\u00a72[+] \u00a7e" + formattedName + "\u00a7e joined!"
                );

			case "\u0123quit":
                return runFormatAndStore(message,
                        QUIT_FORMAT,
                        new String[]{
                                plyN, formattedName
                        },
                        "\u00a74[-] \u00a7e" + formattedName + "\u00a7e disconnected!"
                );

			default:
				if (messageStr.charAt(0) == '/') {
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
                    return CommandSystem.instance.runCommand(message, commandName, argStr);
				}
				else if (messageStr.startsWith("\u0123kick ")) {
					final String param = messageStr.substring(6);
                    return runFormatAndStore(message,
                            KICK_FORMAT,
                            new String[] {
                                    plyN, formattedName, param
                            },
                            "\u00a74[-] \u00a7e" + formattedName + "\u00a7e was kicked (" + messageStr.substring(6) + ")!"
                    );
				}
				else {
                    if(ConvCommand.handleConvMessage(message, formattedName, messageStr, false))
                        return null;

                    return runFormatAndStore(message,
                            MESSAGE_FORMAT,
                            new String[] {
                                    plyN, formattedName, messageStr
                            },
                            formattedName + "\u00a7f: " + messageStr
                    );
				}
		}
	}
}
