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
import com.foxelbox.foxbukkit.chatlink.commands.system.CommandSystem;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;

import java.util.Map;
import java.util.PriorityQueue;

@ICommand.Names({"help", "?", "h"})
@ICommand.Help("Prints a list of available commands or information about the specified command.")
@ICommand.Usage("[<command>]")
@ICommand.Permission("foxbukkit.help")
public class HelpCommand extends ICommand {
    @Override
    public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        Map<String, ICommand> commands = CommandSystem.instance.getCommands();

        final Player commandSender = Player.getPlayerFromMessage(messageIn);
        ChatMessageOut message = makeReply(messageIn);

        if(args.length > 0) {
            ICommand val = commands.get(args[0]);
            if (val == null || !val.canPlayerUseCommand(commandSender)) {
                throw new CommandException("No help for that command available!");
            }

            for (String line : val.getHelp().split("\n")) {
                message.setContentsPlain("\u00a75[FBCL]\u00a7f " + line);
                RedisHandler.sendMessage(message);
            }
            message.setContentsPlain("\u00a75[FBCL]\u00a7f Usage: /" + args[0] + " " + val.getUsage());
            return message;
        }
        else {
            String ret = "Available commands: /";
            for (String key : new PriorityQueue<>(commands.keySet())) {
                if (key.equals("\u00a7"))
                    continue;

                ICommand val = commands.get(key);
                if (!val.canPlayerUseCommand(commandSender))
                    continue;

                ret += key + ", /";
            }
            ret = ret.substring(0,ret.length() - 3);
            message.setContentsPlain("\u00a75[FBCL]\u00a7f " + ret);
            return message;
        }
    }
}
