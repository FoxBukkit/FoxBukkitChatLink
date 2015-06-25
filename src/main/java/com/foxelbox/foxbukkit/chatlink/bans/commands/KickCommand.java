package com.foxelbox.foxbukkit.chatlink.bans.commands;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PermissionDeniedException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

@ICommand.Names("kick")
@ICommand.Help("Kicks the specified user")
@ICommand.Usage("<name>")
@ICommand.Permission("foxbukkit.bans.kick")
public class KickCommand extends ICommand {
    @Override
    public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final Player commandSender = Player.getPlayerFromMessage(messageIn);

        final Player otherPly = PlayerHelper.matchPlayerSingle(args[0], false);

        if (commandSender.getLevel() <= otherPly.getLevel()) {
            throw new PermissionDeniedException();
        }

        otherPly.kick("\u00a7r[" + messageIn.from.name + "] " + Utils.concatArray(" ", args, 1, ""));

        ChatMessageOut message = new ChatMessageOut(messageIn);
        message.setContentsPlain("\u00a75[FBCL]\u00a7f " + messageIn.from.name + " kicked " + otherPly.getName());
        return message;
    }
}
