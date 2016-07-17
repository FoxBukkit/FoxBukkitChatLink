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

        Main.redisManager.publish("playerRankUpdate", commandSender.getUniqueId().toString());

        ChatMessageOut reply = makeReply(messageIn);
        reply.finalizeContext = true;
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f Accounts linked");
        return reply;
    }
}