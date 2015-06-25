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
package com.foxelbox.foxbukkit.chatlink.commands.system;

import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PermissionDeniedException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandSystem {
	private final Map<String,ICommand> commands = new HashMap<>();

    public static final CommandSystem instance = new CommandSystem();

	private CommandSystem() { }

	public void scanCommands() {
		commands.clear();
		scanCommands("com.foxelbox.foxbukkit.chatlink.commands");
		scanCommands("com.foxelbox.foxbukkit.chatlink.permissions.commands");
		scanCommands("com.foxelbox.foxbukkit.chatlink.bans.commands");
        Main.redisManager.del("chatLinkCommands");
        final Set<String> commandsKeySet = commands.keySet();
        Main.redisManager.lpush("chatLinkCommands", commandsKeySet.toArray(new String[commandsKeySet.size()]));
    }

	public void scanCommands(String packageName) {
		for (Class<? extends ICommand> commandClass : Utils.getSubClasses(ICommand.class, packageName)) {
			try {
				commandClass.newInstance();
			}
			catch (InstantiationException e) {
				// We try to instantiate an interface
				// or an object that does not have a
				// default constructor
				continue;
			}
			catch (IllegalAccessException e) {
				// The class/ctor is not public
				continue;
			}
			catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	public void registerCommand(String name, ICommand command) {
		commands.put(name, command);
	}

	public Map<String,ICommand> getCommands() {
		return commands;
	}

	public ChatMessageOut runCommand(ChatMessageIn message, String cmd, String argStr) {
		if (commands.containsKey(cmd)) {
            final Player player = Player.getPlayerFromMessage(message);
			final String playerName = player.getName();
			final ICommand icmd = commands.get(cmd);
			try {
				if(!icmd.canPlayerUseCommand(player))
                    throw new PermissionDeniedException();

				if(needsLogging(player, icmd))
				{
					String logmsg = "FBCL Command: " + playerName + ": "  + cmd + " " + argStr;
                    System.err.println(logmsg);
				}
				return icmd.run(message, PlayerHelper.getFullPlayerName(message.from.uuid, message.from.name), argStr.trim());
			}
			catch (PermissionDeniedException e) {
				String logmsg = "FBCL Command denied: " + playerName + ": "  + cmd + " " + argStr;
				System.err.println(logmsg);
                return ICommand.makeError(message, e.getMessage());
			}
			catch (CommandException e) {
                return ICommand.makeError(message, e.getMessage());
			}
			catch (Exception e) {
				if (player.hasPermission("foxbukkit.detailederrors")) {
					e.printStackTrace();
                    return ICommand.makeError(message, "Command error: "+e+" in "+e.getStackTrace()[0]);
				}
				else {
                    return ICommand.makeError(message, "Command error!");
				}
			}
		}
		return ICommand.makeError(message, "Command not found!");
	}

	private boolean needsLogging(Player commandSender, ICommand command) {
		final Class<? extends ICommand> cls = command.getClass();
		if (cls.isAnnotationPresent(ICommand.NoLogging.class))
			return false;

		return true;
	}
}
