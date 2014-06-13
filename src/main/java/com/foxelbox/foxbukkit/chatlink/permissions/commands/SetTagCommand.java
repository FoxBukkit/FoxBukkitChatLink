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
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PermissionDeniedException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

@ICommand.Names("settag")
@ICommand.Help(
		"Sets the tag of the specified user.\n" +
		"Colors: \u00a70$0 \u00a71$1 \u00a72$2 \u00a73$3 \u00a74$4 \u00a75$5 \u00a76$6 \u00a77$7 \u00a78$8 \u00a79$9 \u00a7a$a \u00a7b$b \u00a7c$c \u00a7d$d \u00a7e$e \u00a7f$f\n" +
		"-r to specify a custom rank tag instead of a player tag."
)
@ICommand.Usage("<name> <tag>|none")
@ICommand.BooleanFlags("rf")
@ICommand.Permission("foxbukkit.users.settag")
public class SetTagCommand extends ICommand {
    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
		args = parseFlags(args);

        final Player commandSender = new Player(message.from);

		final Player otherPly = PlayerHelper.matchPlayerSingle(args[0], false);

		final String newTag = Utils.concatArray(" ", args, 1, "").replace('$', '\u00a7');
		if (PlayerHelper.getPlayerLevel(commandSender.uuid) < PlayerHelper.getPlayerLevel(otherPly.uuid))
			throw new PermissionDeniedException();

		final boolean useRankTag = booleanFlags.contains('r');
		final boolean force = booleanFlags.contains('f');
		final String tagTypeName = useRankTag ? "rank tag" : "tag";

		final String previousTag = PlayerHelper.getPlayerTagRaw(otherPly.getUniqueId(), useRankTag);

		final String undoCommand;
		if (previousTag == null)
			undoCommand = String.format("/%s \"%s\" none", useRankTag ? "settag -r" : "settag", otherPly.getName());
		else {
			undoCommand = String.format("/%s \"%s\" %s", useRankTag ? "settag -r" : "settag", otherPly.getName(), previousTag.replace('\u00a7', '$'));
		}

		if (newTag.equals("none")) {
			PlayerHelper.setPlayerTag(otherPly.getUniqueId(), null, useRankTag);
			return SetNickCommand.announceTagChange(message.server, "%1$s reset "+tagTypeName+" of %2$s!", "%2$s reset their own "+tagTypeName+"!", commandSender, otherPly, undoCommand);
		}
		else if (!useRankTag && !force && newTag.matches("^.*\u00a7.$")) {
			throw new CommandException("Player tag ends in color code. This belongs into the rank tag now (-r flag).");
		}
		else {
            PlayerHelper.setPlayerTag(otherPly.getUniqueId(), newTag, useRankTag);
			return SetNickCommand.announceTagChange(message.server, "%1$s set "+tagTypeName+" of %2$s!", "%2$s set their own "+tagTypeName+"!", commandSender, otherPly, undoCommand);
		}
	}
}
