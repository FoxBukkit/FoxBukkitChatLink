package com.foxelbox.foxbukkit.chatlink.bans.commands;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.bans.Bans;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;

@ICommand.Names({"unban", "pardon"})
@ICommand.Help("Unbans specified user")
@ICommand.Usage("<full name>")
@ICommand.Permission("foxbukkit.bans.unban")
public class UnbanCommand extends ICommand {
	@Override
	public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
		final Player commandSender = Player.getPlayerFromMessage(messageIn);
		Bans.instance.unban(commandSender, args[0]);
        return null;
	}
}
