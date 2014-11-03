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

import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;

@ICommand.Names({"opchat"})
@ICommand.Help("Sends message to op chat.")
@ICommand.Usage("<text>")
@ICommand.Permission("foxbukkit.opchat")
public class OpChatCommand extends ICommand {
    private static final String OPCHAT_FORMAT = "<color name=\"yellow\">[#OP]</color> " + RedisHandler.MESSAGE_FORMAT;

    @Override
    public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String argStr) throws CommandException {
        final ChatMessageOut message = new ChatMessageOut(messageIn);
        message.contents = new MessageContents("\u00a7e[#OP] \u00a7f" + formattedName + "\u00a7f: " + argStr,
                OPCHAT_FORMAT,
                new String[] {
                        messageIn.from.name, formattedName, argStr
                });
        message.to.type = "permission";
        message.importance = 3;
        message.to.filter = new String[] { "foxbukkit.opchat" };
        return message;
    }
}
