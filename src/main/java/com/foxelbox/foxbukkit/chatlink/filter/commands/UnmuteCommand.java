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
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.MessageTarget;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

@ICommand.Names("unmute")
@ICommand.Help("Unmutes target globally")
@ICommand.Usage("<target>")
@ICommand.Permission("foxbukkit.filter.mute")
public class UnmuteCommand extends ICommand {
    @Override
    public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final Player target = PlayerHelper.matchPlayerSingle(args[0], false);
        target.isMuted = false;
        ChatMessageOut reply = makeReply(messageIn);
        reply.to = new MessageTarget("all", null);
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f " + sender.getName() + " unmuted " + target.getName());
        return reply;
    }
}
