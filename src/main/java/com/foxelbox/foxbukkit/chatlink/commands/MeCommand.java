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

@ICommand.Names("me")
@ICommand.Help("Well, it's /me, durp")
@ICommand.Usage("<stuff here>")
@ICommand.Permission("foxbukkit.communication.emote")
public class MeCommand extends ICommand {
    public static final String EMOTE_FORMAT = "* " + RedisHandler.PLAYER_FORMAT + " <color name=\"gray\">%4$s</color>";

    @Override
    public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String argStr) {
        if(ConvCommand.handleConvMessage(messageIn, formattedName, argStr, true))
            return null;

        final ChatMessageOut message = new ChatMessageOut(messageIn);

        message.setContents(
                EMOTE_FORMAT,
                new String[] {
                        messageIn.from.name, messageIn.from.uuid.toString(), formattedName, argStr
                });
        return message;
    }
}
