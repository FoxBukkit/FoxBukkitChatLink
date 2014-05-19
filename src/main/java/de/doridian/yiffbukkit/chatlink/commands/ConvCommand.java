package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;

public class ConvCommand extends ICommand {
    private static final String CONV_FORMAT = "<color name=\"yellow\">[CONV]</color> " + RedisHandler.MESSAGE_FORMAT;

    @Override
    public String[] getNames() {
        return new String[] { "conv" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String argStr) {
        message.contents = new MessageContents("\u00a7f* " + formattedName + "\u00a77 " + argStr,
                CONV_FORMAT,
                new String[] {
                        message.from.name, formattedName, argStr
                });
        return message;
    }
}
