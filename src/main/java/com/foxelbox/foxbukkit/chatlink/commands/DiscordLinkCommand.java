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
            throw new CommandException("Invalid discord link code");
        }

        String[] split = args[0].split("_");
        if(split.length != 2) {
            throw new CommandException("Invalid discord link code");
        }

        final String discordId = split[0];
        long gotHash = Long.parseLong(split[1]);
        CRC32 correctHash = new CRC32();
        correctHash.update(Main.configuration.getValue("discord-hash-secret", "").getBytes());
        correctHash.update(new byte[] { '_' });
        correctHash.update((discordId.getBytes()));
        if(gotHash != correctHash.getValue()) {
            throw new CommandException("Invalid discord link code");
        }

        Main.redisManager.hset("discordlinks:discord-to-mc", discordId, commandSender.getUniqueId().toString());
        Main.redisManager.hset("discordlinks:mc-to-discord", commandSender.getUniqueId().toString(), discordId);

        ChatMessageOut reply = new ChatMessageOut(messageIn);
        reply.finalizeContext = true;
        reply.setContentsPlain("Accounts linked");
        return reply;
    }
}