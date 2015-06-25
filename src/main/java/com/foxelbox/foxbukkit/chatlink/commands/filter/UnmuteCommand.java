package com.foxelbox.foxbukkit.chatlink.commands.filter;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

@ICommand.Names("unmute")
@ICommand.Help("Unmute player")
@ICommand.Usage("")
@ICommand.Permission("foxbukkit.filter.mute")
public class UnmuteCommand extends ICommand {
    @Override
    public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final Player target = PlayerHelper.matchPlayerSingle(args[0], false);
        target.isMuted = false;
        ChatMessageOut reply = makeReply(messageIn);
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f Unmuted " + target.getName());
        return reply;
    }
}
