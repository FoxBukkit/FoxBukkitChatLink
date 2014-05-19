package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;

public class MeCommand extends ICommand {
    private static final String EMOTE_FORMAT = "* " + RedisHandler.PLAYER_FORMAT + " <color name=\"gray\">%3$s</color>";

    @Override
    public String[] getNames() {
        return new String[] { "me", "emote" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String argStr) {
        message.contents = new MessageContents("\u00a7f* " + formattedName + "\u00a77 " + argStr,
                EMOTE_FORMAT,
                new String[] {
                        message.from.name, formattedName, argStr
                });
        return message;
    }
}
