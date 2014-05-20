package de.doridian.yiffbukkit.chatlink;

import com.google.gson.Gson;
import de.doridian.dependencies.redis.AbstractRedisHandler;
import de.doridian.dependencies.redis.RedisManager;
import de.doridian.yiffbukkit.chatlink.commands.*;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;
import de.doridian.yiffbukkit.chatlink.json.UserInfo;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;
import redis.clients.jedis.Protocol;

import java.util.*;

public class RedisHandler extends AbstractRedisHandler {
    public RedisHandler() {
        super(Main.redisManager, "yiffbukkit:from_server");
    }

	public static final String PLAYER_FORMAT = "<span onClick=\"suggest_command('/pm %1$s ')\">%2$s</span>";
    public static final String MESSAGE_FORMAT = PLAYER_FORMAT + "<color name=\"white\">: %3$s</color>";
    public static final String KICK_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">was kicked (%3$s)!</color>";
    public static final String QUIT_FORMAT = "<color name=\"dark_red\">[-]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">disconnected!</color>";
    public static final String JOIN_FORMAT = "<color name=\"dark_green\">[+]</color> " + PLAYER_FORMAT + " <color name=\"yellow\">joined!</color>";

    private static final Gson gson = new Gson();

    private static final Map<String, ICommand> commandMap;

    static {
        commandMap = new HashMap<>();
        __addCommand(new MeCommand());
        __addCommand(new ConvCommand());
        __addCommand(new PMCommand());
        __addCommand(new OpChatCommand());
    }

    private static void __addCommand(ICommand command) {
        for(String name : command.getNames())
            commandMap.put(name.toLowerCase(), command);
    }

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
        Main.redisManager.publish("yiffbukkit:to_server", outMsg);
    }

    private static ChatMessage runFormatAndStore(ChatMessage message, String format, String[] formatArgs, String plain) {
        message.contents = new MessageContents(plain, format, formatArgs);
        return message;
    }

	private static ChatMessage formatMessage(ChatMessage message) {
        final String plyN = message.from.name;
		final String formattedName = PlayerHelper.getFullPlayerName(message.from.uuid, plyN);

        String messageStr = message.contents.plain;

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
                    final ICommand command;
                    synchronized (commandMap) {
                        command = commandMap.get(commandName.toLowerCase());
                    }
                    if(command != null) {
                        try {
                            return command.run(message, formattedName, argStr);
                        } catch (CommandException e) {
                            return ICommand.makeError(message, e.getMessage());
                        }
                    }
					return ICommand.makeError(message, "Unknown command");
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
