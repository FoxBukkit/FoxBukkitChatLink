package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;
import de.doridian.yiffbukkit.chatlink.util.CommandException;

public class OpChatCommand extends ICommand {
    private static final String OPCHAT_FORMAT = "<color name=\"yellow\">[#OP]</color> " + RedisHandler.MESSAGE_FORMAT;

    @Override
    public String[] getNames() {
        return new String[] { "opchat" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String argStr) throws CommandException {
        message.contents = new MessageContents("\u00a7e[#OP] \u00a7f" + formattedName + "\u00a7f: " + argStr,
                OPCHAT_FORMAT,
                new String[] {
                        message.from.name, formattedName, argStr
                });
        message.to.type = "permission";
        message.to.filter = new String[] { "yiffbukkit.opchat" };
        return message;
    }
}
