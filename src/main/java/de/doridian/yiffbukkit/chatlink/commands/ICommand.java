/**
 * This file is part of YiffBukkitChatLink.
 *
 * YiffBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class ICommand {
    public abstract String[] getNames();

    public static ChatMessage makeReply(ChatMessage message) {
        message.to.type = "player";
        message.to.filter = new String[] { message.from.uuid.toString() };
        return message;
    }

    public static ChatMessage makeError(ChatMessage message, String error) {
        message = makeReply(message);
        message.contents.plain = "\u00a74[YBCL] " + error;
        message.contents.xml_format = "<color name=\"dark_red\">[YBCL] " + error + "</color>";
        message.contents.xml_format_args = null;
        return message;
    }

    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        throw new CommandException("Not implemented");
    }

    public ChatMessage run(ChatMessage message, String formattedName, String argStr) throws CommandException {
        return run(message, formattedName, argStr.split(" "));
    }
}
