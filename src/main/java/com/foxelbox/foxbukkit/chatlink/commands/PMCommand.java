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

import com.foxelbox.foxbukkit.chatlink.ChatQueueHandler;
import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.Messages;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.filter.MuteList;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.MessageTarget;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

@ICommand.Names({"pm", "msg", "tell"})
@ICommand.Help("Sends a private message to the specified user, that cannot be seen by anyone but the target and yourself.")
@ICommand.Usage("<name> <text>")
@ICommand.Permission("foxbukkit.communication.pm")
@ICommand.NoLogging
public class PMCommand extends ICommand {
    private static final String PM_SEND_FORMAT = "<color name=\"yellow\">[PM &gt;]</color> " + ChatQueueHandler.MESSAGE_FORMAT;
    private static final String PM_RECEIVE_FORMAT = "<color name=\"yellow\">[PM &lt;]</color> " + ChatQueueHandler.MESSAGE_FORMAT;

    @Override
    public ChatMessageOut run(Player commandSender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        if(MuteList.isMuted(commandSender)) {
            return null;
        }

        final String messageText = Utils.concatArray(" ", args, 1, "");
        final Player target = PlayerHelper.matchPlayerSingle(args[0]);

        ChatMessageOut message = new ChatMessageOut(messageIn);

        message.setContents(
                PM_RECEIVE_FORMAT,
                new String[] {
                        messageIn.from.name, message.from.uuid.toString(), formattedName, messageText
                });
        message.to = new MessageTarget(Messages.TargetType.PLAYER, new String[] { target.getUniqueId().toString() });
        message.importance = 4;
        Main.chatQueueHandler.sendMessage(message);

        formattedName = PlayerHelper.getFullPlayerName(target.getUniqueId(), target.getName());

		message = makeReply(messageIn);
        message.setContents(
                PM_SEND_FORMAT,
                new String[] {
                        target.getName(), target.getUniqueId().toString(), formattedName, messageText
                });
        message.importance = 4;
        return message;
    }
}
