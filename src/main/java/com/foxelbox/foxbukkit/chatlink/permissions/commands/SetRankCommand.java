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
package com.foxelbox.foxbukkit.chatlink.permissions.commands;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.permissions.FoxBukkitPermissionHandler;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PermissionDeniedException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.UUID;

@ICommand.Names("setrank")
@ICommand.Help("Sets rank of specified user")
@ICommand.Usage("<full name> <rank>")
@ICommand.BooleanFlags("p")
@ICommand.Permission("foxbukkit.users.setrank")
public class SetRankCommand extends ICommand {
	@Override
    public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        Player commandSender = Player.getPlayerFromMessage(messageIn);
        args = parseFlags(args);

		String otherName = args[0];
		Player otherPly = new Player(UUID.fromString(PlayerHelper.playerNameToUUID.get(args[0].toLowerCase())));
		String newRank = args[1];
		String oldRank = PlayerHelper.getPlayerRank(otherPly.getUniqueId());
		
		if(oldRank.equalsIgnoreCase("banned")) {
			throw new CommandException("Player is banned! /unban first!");
		}
		
		if(newRank.equalsIgnoreCase("banned")) {
			throw new CommandException("Please use /ban to ban people!");
		}

		if (newRank.equalsIgnoreCase(oldRank))
			throw new CommandException("Player already has that rank!");

		if(!PlayerHelper.rankLevels.containsKey(newRank)) {
			throw new CommandException("Rank does not exist!");
		}

		int selflvl = commandSender.getLevel();
		int oldlvl = otherPly.getLevel();
		int newlvl = PlayerHelper.getRankLevel(newRank);

		if(selflvl <= oldlvl)
			throw new PermissionDeniedException();

		if(selflvl <= newlvl)
			throw new PermissionDeniedException();

		int opLvl = PlayerHelper.getRankLevel("op");

		if(PlayerHelper.getRankLevel(newRank) >= opLvl && !commandSender.hasPermission("foxbukkit.users.makestaff"))
			throw new PermissionDeniedException();
		
		if(newlvl >= opLvl && !commandSender.hasPermission("foxbukkit.users.modifystaff"))
			throw new PermissionDeniedException();

		if(booleanFlags.contains('p') && newlvl < oldlvl)
			throw new PermissionDeniedException();

        FoxBukkitPermissionHandler.instance.setGroup(otherPly.getUniqueId(), newRank);

        ChatMessageOut message = new ChatMessageOut(messageIn);
        message.contents = new MessageContents("\u00a75[FBCL]\u00a7f " + messageIn.from.name + " set rank of " + otherName + " to " + newRank);
        return message;
	}
}
