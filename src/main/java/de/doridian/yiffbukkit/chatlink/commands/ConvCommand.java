package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.Player;
import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;
import de.doridian.yiffbukkit.chatlink.json.MessageTarget;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConvCommand extends ICommand {
    private static final String CONV_FORMAT = "<color name=\"yellow\">[CONV]</color> " + RedisHandler.MESSAGE_FORMAT;
    private static final String CONV_EMOTE_FORMAT = "<color name=\"yellow\">[CONV]</color> " + MeCommand.EMOTE_FORMAT;

    @Override
    public String[] getNames() {
        return new String[] { "conv" };
    }

    private static final Map<UUID, UUID> conversationMap = new HashMap<>();

    public static boolean handleConvMessage(ChatMessage message, String formattedName, String messageText, boolean isEmote) {
        UUID targetUUID = conversationMap.get(message.from.uuid);
        if(targetUUID == null)
            return false;

        Player target = new Player(targetUUID);

        if(!target.isOnline()) {
            message.to.type = "player";
            message.to.filter = new String[] { message.from.uuid.toString() };
            message.contents.plain = "\u00a74[YBCL] Conversation target is not online";
            message.contents.xml_format = "<color name=\"dark_red\">[YBCL] Conversation target is not online</color>";
            message.contents.xml_format_args = null;
            RedisHandler.sendMessage(message);
            return true;
        }

        if(isEmote) {
            message.contents = new MessageContents("\u00a7e[CONV] \u00a7f* " + formattedName + "\u00a77 " + messageText,
                    CONV_EMOTE_FORMAT,
                    new String[]{
                            message.from.name, formattedName, messageText
                    }
            );
        } else {
            message.contents = new MessageContents("\u00a7e[CONV] \u00a7f" + formattedName + ": " + messageText,
                    CONV_FORMAT,
                    new String[]{
                            message.from.name, formattedName, messageText
                    }
            );
        }
        message.to = new MessageTarget("player", new String[] { target.uuid.toString(), message.from.uuid.toString() });
        RedisHandler.sendMessage(message);

        return true;
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        message.to.type = "player";
        message.to.filter = new String[] { message.from.uuid.toString() };
        if(args.length > 0 && !args[0].isEmpty()) {
            Player target = PlayerHelper.matchPlayerSingle(args[0]);
            conversationMap.put(message.from.uuid, target.uuid);
            message.contents = new MessageContents("\u00a75[YBCL] \u00a7fStarted conversation with " + target.name);
        } else {
            conversationMap.remove(message.from.uuid);
            message.contents = new MessageContents("\u00a75[YBCL] \u00a7fClosed conversation");
        }
        return message;
    }
}
