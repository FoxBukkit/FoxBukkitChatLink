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

import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.permissions.FoxBukkitPermissionHandler;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;

@ICommand.Names("creloadpermissions")
@ICommand.Help("Reloads the permissions system.")
@ICommand.Usage("")
@ICommand.Permission("foxbukkit.reloadpermissions")
public class ReloadPermissionsCommand extends ICommand {
    @Override
    public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String argStr) throws CommandException {
        FoxBukkitPermissionHandler.instance.reload();
        ChatMessageOut message = makeReply(messageIn);
        message.setContentsPlain("\u00a75[FBCL] \u00a7fPermissions system reloaded!");
        return message;
    }
}
