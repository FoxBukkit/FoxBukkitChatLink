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
package com.foxelbox.foxbukkit.chatlink.commands;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessage;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.json.MessageTarget;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ICommand.Names({"conv", "conversation"})
@ICommand.Help("Opens or closes a conversation with the given player. This means that all your chat is going to them until you close the conversation by running the command without parameters.")
@ICommand.Usage("[<name>]")
@ICommand.Permission("foxbukkit.communication.conversation")
@ICommand.NoLogging
public class ConvCommand extends ICommand {
    private static final String CONV_FORMAT = "<color name=\"yellow\">[CONV]</color> " + RedisHandler.MESSAGE_FORMAT;
    private static final String CONV_EMOTE_FORMAT = "<color name=\"yellow\">[CONV]</color> " + MeCommand.EMOTE_FORMAT;

    private static final Map<UUID, UUID> conversationMap = new HashMap<>();

    public static boolean handleConvMessage(ChatMessage message, String formattedName, String messageText, boolean isEmote) {
        UUID targetUUID = conversationMap.get(message.from.uuid);
        if(targetUUID == null)
            return false;

        Player target = new Player(targetUUID);

        if(!target.isOnline()) {
            RedisHandler.sendMessage(makeError(message, "Conversation target is not online"));
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
            message.contents = new MessageContents("\u00a7e[CONV] \u00a7f" + formattedName + "\u00a7f: " + messageText,
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
        makeReply(message);
        if(args.length > 0) {
            Player target = PlayerHelper.matchPlayerSingle(args[0]);
            conversationMap.put(message.from.uuid, target.uuid);
            message.contents = new MessageContents("\u00a75[FBCL] \u00a7fStarted conversation with " + target.name);
        } else {
            conversationMap.remove(message.from.uuid);
            message.contents = new MessageContents("\u00a75[FBCL] \u00a7fClosed conversation");
        }
        return message;
    }
}
