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

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.bans.Bans;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PermissionDeniedException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

@ICommand.Names("ban")
@ICommand.Help(
		"Bans specified user. Specify offline players in quotation marks.\n"+
		"Flags:\n"+
		"  -r to rollback\n"+
		"  -g to issue an bans.com global ban\n"+
		"  -t <time> to issue a temporary ban. Possible suffixes:\n"+
		"       m=minutes, h=hours, d=days"
)
@ICommand.Usage("[<flags>] <name> [reason here]")
@ICommand.BooleanFlags("jrg")
@ICommand.StringFlags("t")
@ICommand.Permission("foxbukkit.bans.ban")
public class BanCommand extends ICommand {
	@Override
	public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
		args = parseFlags(args);
		executeBan(messageIn.server, sender, args[0], Utils.concatArray(" ", args, 1, null), booleanFlags.contains('r'), booleanFlags.contains('g'), stringFlags.get('t'));
		return makeBlank(messageIn);
	}

	public static void executeBan(String server, Player commandSender, String plyName, String reason, boolean rollback, boolean global, final String duration) throws CommandException {
		if (!commandSender.hasPermission("foxbukkit.users.ban")) throw new PermissionDeniedException();

		final Player otherply = PlayerHelper.matchPlayerSingle(plyName, false);

		if (PlayerHelper.getPlayerLevel(commandSender.getUniqueId()) <= PlayerHelper.getPlayerLevel(otherply.getUniqueId()))
			throw new PermissionDeniedException();

		/*if (rollback) { TODO: THIS
			asPlayer(commandSender).chat("/lb writelogfile player "+otherply.getName());
		}*/

		if (reason == null) {
			reason = "Kickbanned by " + commandSender.getName();
		}

		final Bans.BanType type;
		if (duration != null) {
			if (global)
				throw new CommandException("Bans can only be either global or temporary");
			type = Bans.BanType.TEMPORARY;

			if (duration.length() < 2)
				throw new CommandException("Malformed ban duration");

			final String measure = duration.substring(duration.length() - 1);

			final long durationValue;
			try {
				durationValue = Long.parseLong(duration.substring(0, duration.length() - 2).trim());
			}
			catch (NumberFormatException e) {
				throw new CommandException("Malformed ban duration");
			}

			Bans.instance.ban(commandSender, otherply, reason, type, durationValue, measure);
		}
		else {
			if (global) {
				type = Bans.BanType.GLOBAL;
			} else {
				type = Bans.BanType.LOCAL;
			}

			Bans.instance.ban(commandSender, otherply, reason, type);
		}

		/*if (rollback) { TODO: THIS
			asPlayer(commandSender).chat("/lb rollback player "+otherply.getName());
		}*/

		otherply.kick("[" + commandSender.getName() + "] " + reason);
	}
}
