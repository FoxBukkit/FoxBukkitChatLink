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
import com.foxelbox.foxbukkit.chatlink.json.ChatMessage;
import com.foxelbox.foxbukkit.chatlink.util.*;

@ICommand.Names("setnick")
@ICommand.Help(
		"Sets the nick of the specified user.\n" +
		"Colors: \u00a70$0 \u00a71$1 \u00a72$2 \u00a73$3 \u00a74$4 \u00a75$5 \u00a76$6 \u00a77$7 \u00a78$8 \u00a79$9 \u00a7a$a \u00a7b$b \u00a7c$c \u00a7d$d \u00a7e$e \u00a7f$f"
)
@ICommand.Usage("<name> <nick>|none")
@ICommand.Permission("foxbukkit.users.setnick")
public class SetNickCommand extends ICommand {
    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
		final Player otherPly = PlayerHelper.matchPlayerSingle(args[0], false);

		final String newNick = Utils.concatArray(" ", args, 1, "").replace('$', '\u00a7');
		if (Player.getPlayerFromMessage(message).getLevel() < otherPly.getLevel())
			throw new PermissionDeniedException();

		final String undoCommand;
		if (otherPly.getName().equals(otherPly.getDisplayName()))
			undoCommand = String.format("/setnick \"%s\" none", otherPly.getName());
		else
			undoCommand = String.format("/setnick \"%s\" %s", otherPly.getName(), otherPly.getDisplayName().replace('\u00a7', '$'));

		if (newNick.equals("none")) {
			PlayerHelper.setPlayerNick(otherPly.getUniqueId(), null);
			return announceTagChange(message.server, "%1$s reset nickname of %2$s!", "%2$s reset their own nickname!", Player.getPlayerFromMessage(message), otherPly, undoCommand);
		}
		else {
            PlayerHelper.setPlayerNick(otherPly.getUniqueId(), newNick);
			return announceTagChange(message.server, "%1$s set nickname of %2$s!", "%2$s set their own nickname!", Player.getPlayerFromMessage(message), otherPly, undoCommand);
		}
	}

	public static ChatMessage announceTagChange(String server, String formatOther, String formatOwn, Player commandSender, Player modifiedPlayer, String undoCommand) {
		final String format;
		if (commandSender == modifiedPlayer)
			format = formatOwn;
		else
			format = formatOther;

		ChatMessage message = MessageHelper.sendServerMessage(String.format(
                format + " %3$s",
                MessageHelper.format(commandSender),
                MessageHelper.formatWithTag(modifiedPlayer),
                MessageHelper.button(undoCommand, "undo", "blue", false)
        ));
        message.contents.plain = "\u00a75[FBCL]\u00a7f " + String.format(format, MessageHelper.format(commandSender), MessageHelper.formatWithTag(commandSender));
        message.from.uuid = commandSender.getUniqueId();
        message.from.name = commandSender.getName();
        message.server = server;
        return message;
    }
}
