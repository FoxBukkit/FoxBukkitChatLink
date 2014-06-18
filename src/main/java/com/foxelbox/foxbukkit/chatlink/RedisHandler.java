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
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.google.gson.Gson;
import com.foxelbox.dependencies.redis.AbstractRedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.*;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.json.UserInfo;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.*;
import java.util.regex.Pattern;

public class RedisHandler extends AbstractRedisHandler {
    public RedisHandler() {
        super(Main.redisManager, "foxbukkit:from_server");
    }

    private static final Pattern REMOVE_COLOR_CODE = Pattern.compile("\u00a7.");
    private static final Pattern REMOVE_DISALLOWED_CHARS = Pattern.compile("[\u00a7\r\n\t]");

	public static final String PLAYER_FORMAT = "<span onClick=\"suggest_command('/pm %1$s ')\">%2$s</span>";
    public static final String MESSAGE_FORMAT = PLAYER_FORMAT + "<color name=\"white\">: %3$s</color>";
    public static final String KICK_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">was kicked (%3$s)!</color>";
    public static final String QUIT_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">disconnected!</color>";
    public static final String JOIN_FORMAT = "<color name=\"dark_green\">[+]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">joined!</color>";

    private static final Gson gson = new Gson();

	@Override
	public void onMessage(final String c_message) {
		try {
            final ChatMessageIn chatMessageIn;
            synchronized (gson) {
                chatMessageIn = gson.fromJson(c_message, ChatMessageIn.class);
            }

            ChatMessageOut message = formatMessage(chatMessageIn);

            if(message == null)
                return;

            sendMessage(message);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void sendMessage(ChatMessageOut message) {
        final String outMsg;
        synchronized (gson) {
            outMsg = gson.toJson(message);
        }
        Main.redisManager.publish("foxbukkit:to_server", outMsg);
    }

    private static ChatMessageOut runFormatAndStore(ChatMessageIn messageIn, String format, String[] formatArgs, String plain) {
        ChatMessageOut messageOut = new ChatMessageOut(messageIn);
        messageOut.contents = new MessageContents(plain, format, formatArgs);
        return messageOut;
    }

	private static ChatMessageOut formatMessage(ChatMessageIn messageIn) {
        final String plyN = messageIn.from.name;
        final String formattedName = PlayerHelper.getFullPlayerName(messageIn.from.uuid, plyN);

        String messageStr = REMOVE_DISALLOWED_CHARS.matcher(messageIn.contents).replaceAll("");

        if (messageStr.charAt(0) == '#')
            messageStr = "/opchat " + messageStr.substring(1);

        switch (messageIn.type) {
            case "playerstate":
                switch (messageStr) {
                    case "join":
                        return runFormatAndStore(messageIn,
                                JOIN_FORMAT,
                                new String[]{
                                        plyN, formattedName
                                },
                                "\u00a72[+] \u00a7e" + formattedName + "\u00a7e joined!"
                        );

                    case "quit":
                        return runFormatAndStore(messageIn,
                                QUIT_FORMAT,
                                new String[]{
                                        plyN, formattedName
                                },
                                "\u00a74[-] \u00a7e" + formattedName + "\u00a7e disconnected!"
                        );
                }

                if (messageStr.startsWith("kick ")) {
                    final String param = messageStr.substring(5);
                    return runFormatAndStore(messageIn,
                            KICK_FORMAT,
                            new String[]{
                                    plyN, formattedName, param
                            },
                            "\u00a74[-] \u00a7e" + formattedName + "\u00a7e was kicked (" + messageStr.substring(6) + ")!"
                    );
                }

                break;

            case "text":
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
                    return CommandSystem.instance.runCommand(messageIn, commandName, argStr);
                }
                else {
                    if(ConvCommand.handleConvMessage(messageIn, formattedName, messageStr, false))
                        return null;

                    return runFormatAndStore(messageIn,
                            MESSAGE_FORMAT,
                            new String[] {
                                    plyN, formattedName, messageStr
                            },
                            formattedName + "\u00a7f: " + messageStr
                    );
                }
        }

        throw new RuntimeException("Unprocessable message: " + messageIn.type + " => " + messageStr);
	}
}
