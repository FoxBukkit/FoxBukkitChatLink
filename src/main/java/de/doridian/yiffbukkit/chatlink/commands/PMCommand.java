package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.Player;
import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;
import de.doridian.yiffbukkit.chatlink.json.MessageTarget;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;
import de.doridian.yiffbukkit.chatlink.util.Utils;

public class PMCommand extends ICommand {
    private static final String PM_SEND_FORMAT = "<color name=\"yellow\">[PM &gt;]</color> " + RedisHandler.MESSAGE_FORMAT;
    private static final String PM_RECEIVE_FORMAT = "<color name=\"yellow\">[PM &lt;]</color> " + RedisHandler.MESSAGE_FORMAT;

    @Override
    public String[] getNames() {
        return new String[] { "pm", "msg", "tell" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        final String messageText = Utils.concatArray(args, 1, "");
        final Player target = PlayerHelper.matchPlayerSingle(args[0]);

        message.contents = new MessageContents("\u00a7f* " + formattedName + "\u00a77 " + messageText,
                PM_RECEIVE_FORMAT,
                new String[] {
                        message.from.name, formattedName, messageText
                });
        message.to = new MessageTarget("player", new String[] { target.uuid.toString() });
        RedisHandler.sendMessage(message);

        message.contents = new MessageContents("\u00a7f* " + formattedName + "\u00a77 " + messageText,
                PM_SEND_FORMAT,
                new String[] {
                        target.name, PlayerHelper.getFullPlayerName(target.uuid, target.name), messageText
                });
        message.to = new MessageTarget("player", new String[] { message.from.uuid.toString() });
        message.from.uuid = target.uuid;
        message.from.name = target.name;
        RedisHandler.sendMessage(message);

        return null;
    }
}
