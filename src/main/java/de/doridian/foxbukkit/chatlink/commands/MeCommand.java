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
package de.doridian.foxbukkit.chatlink.commands;

import de.doridian.foxbukkit.chatlink.RedisHandler;
import de.doridian.foxbukkit.chatlink.json.ChatMessage;
import de.doridian.foxbukkit.chatlink.json.MessageContents;

public class MeCommand extends ICommand {
    public static final String EMOTE_FORMAT = "* " + RedisHandler.PLAYER_FORMAT + " <color name=\"gray\">%3$s</color>";

    @Override
    public String[] getNames() {
        return new String[] { "me", "emote" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String argStr) {
        if(ConvCommand.handleConvMessage(message, formattedName, argStr, true))
            return null;

        message.contents = new MessageContents("\u00a7f* " + formattedName + "\u00a77 " + argStr,
                EMOTE_FORMAT,
                new String[] {
                        message.from.name, formattedName, argStr
                });
        return message;
    }
}
