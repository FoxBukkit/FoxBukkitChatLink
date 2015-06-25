package com.foxelbox.foxbukkit.chatlink.commands.filter;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

@ICommand.Names("mute")
@ICommand.Help("Mutes target")
@ICommand.Usage("")
@ICommand.Permission("foxbukkit.filter.mute")
public class MuteCommand extends ICommand {
    @Override
    public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final Player target = PlayerHelper.matchPlayerSingle(args[0], false);
        target.isMuted = true;
        ChatMessageOut reply = makeReply(messageIn);
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f Muted " + target.getName());
        return reply;
    }
}
