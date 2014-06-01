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
import com.foxelbox.foxbukkit.chatlink.json.ChatMessage;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.json.MessageTarget;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

public class PMCommand extends ICommand {
    private static final String PM_SEND_FORMAT = "<color name=\"yellow\">[PM &gt;]</color> " + RedisHandler.MESSAGE_FORMAT;
    private static final String PM_RECEIVE_FORMAT = "<color name=\"yellow\">[PM &lt;]</color> " + RedisHandler.MESSAGE_FORMAT;

    @Override
    public String[] getNames() {
        return new String[] { "pm", "msg", "tell" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        final String messageText = Utils.concatArray(" ", args, 1, "");
        final Player target = PlayerHelper.matchPlayerSingle(args[0]);

        message.contents = new MessageContents("\u00a7e[PM <] \u00a7f" + formattedName + "\u00a7f: " + messageText,
                PM_RECEIVE_FORMAT,
                new String[] {
                        message.from.name, formattedName, messageText
                });
        message.to = new MessageTarget("player", new String[] { target.uuid.toString() });
        RedisHandler.sendMessage(message);

        formattedName = PlayerHelper.getFullPlayerName(target.uuid, target.name);
        message.contents = new MessageContents("\u00a7e[PM >] \u00a7f" + formattedName + "\u00a7f: " + messageText,
                PM_SEND_FORMAT,
                new String[] {
                        target.name, formattedName, messageText
                });
        message.to = new MessageTarget("player", new String[] { message.from.uuid.toString() });
        message.from.uuid = target.uuid;
        message.from.name = target.name;
        RedisHandler.sendMessage(message);

        return null;
    }
}
