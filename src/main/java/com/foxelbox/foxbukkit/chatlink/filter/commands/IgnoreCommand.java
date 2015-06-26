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
package com.foxelbox.foxbukkit.chatlink.filter.commands;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.filter.IgnoreList;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

@ICommand.Names("ignore")
@ICommand.Help("Ignores target")
@ICommand.Usage("<target>")
@ICommand.Permission("foxbukkit.filter.ignore")
public class IgnoreCommand extends ICommand {
    @Override
    public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final Player target = PlayerHelper.matchPlayerSingle(args[0], false);
        IgnoreList.add(sender.getUniqueId(), target.getUniqueId());
        ChatMessageOut reply = makeReply(messageIn);
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f Ignored " + target.getName());
        return reply;
    }
}
