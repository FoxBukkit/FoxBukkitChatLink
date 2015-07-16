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
package com.foxelbox.foxbukkit.chatlink.bans.commands;

import com.foxelbox.foxbukkit.chatlink.Messages;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PermissionDeniedException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

@ICommand.Names("kick")
@ICommand.Help("Kicks the specified user")
@ICommand.Usage("<name>")
@ICommand.Permission("foxbukkit.bans.kick")
public class KickCommand extends ICommand {
    @Override
    public ChatMessageOut run(Player commandSender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final Player otherPly = PlayerHelper.matchPlayerSingle(args[0], false);

        if (commandSender.getLevel() <= otherPly.getLevel()) {
            throw new PermissionDeniedException();
        }

        otherPly.kick("[" + messageIn.from.name + "] " + Utils.concatArray(" ", args, 1, ""));

        ChatMessageOut reply = makeReply(messageIn);
        reply.to.type = Messages.TargetType.ALL;
        reply.to.filter = null;
        reply.setContentsPlain(messageIn.from.name + " kicked " + otherPly.getName());
        return reply;
    }
}
