package com.foxelbox.foxbukkit.chatlink.commands;

import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;

import java.util.zip.CRC32;

@ICommand.Names("discordlink")
@ICommand.Help("Send /discordlink to the FoxelBot in PM")
@ICommand.Usage("<code>")
@ICommand.Permission("foxbukkit.discordlink")
public class DiscordLinkCommand extends ICommand {
    @Override
    public ChatMessageOut run(Player commandSender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        if(args.length != 1) {
            throw new CommandException("Invalid discord link code parameters");
        }

        final String redisKey = "discordlink:key:" + args[0];
        final String discordId = Main.redisManager.get(redisKey);
        if(discordId == null || discordId.length() < 1) {
            throw new CommandException("Invalid discord link code");
        }
        Main.redisManager.del(redisKey);

        Main.redisManager.hset("discordlinks:discord-to-mc", discordId, commandSender.getUniqueId().toString());
        Main.redisManager.hset("discordlinks:mc-to-discord", commandSender.getUniqueId().toString(), discordId);

        ChatMessageOut reply = new ChatMessageOut(messageIn);
        reply.finalizeContext = true;
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f Accounts linked");
        return reply;
    }
}