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
import com.foxelbox.foxbukkit.chatlink.bans.BanResolver;
import com.foxelbox.foxbukkit.chatlink.bans.LogEntry;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.text.SimpleDateFormat;
import java.util.List;

@ICommand.Names({ "who", "list" })
@ICommand.Help("Prints user list if used without parameters or information about the specified user")
@ICommand.Usage("")
@ICommand.Permission("foxbukkit.who")
public class ListCommand extends ICommand {
    private static final String LIST_FORMAT = "<color name=\"dark_purple\">[FBCL]</color> <color name=\"dark_gray\">[%1$s]</color> %2$s";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    @Override
    public ChatMessageOut run(final Player commandSender, final ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        if(args.length > 0) {
            final Player target = PlayerHelper.matchPlayerSingle(args[0], false);

            ChatMessageOut reply = makeReply(messageIn);

            reply.setContentsPlain("Name: " + target.getName());
            RedisHandler.sendMessage(reply);

            reply.setContentsPlain("Rank: " + PlayerHelper.getPlayerRank(target.getUniqueId()));
            RedisHandler.sendMessage(reply);

            reply.setContentsPlain("NameTag: " + PlayerHelper.getFullPlayerName(target.getUniqueId(), target.getName()));

            if(commandSender.hasPermission("foxbukkit.who.logdetails")) {
                RedisHandler.sendMessage(reply);
                new Thread() {
                    public void run() {
                        ChatMessageOut reply = makeReply(messageIn);
                        LogEntry logEntry = BanResolver.getLatestEntry(commandSender.getName(), commandSender.getUniqueId(), "logout", messageIn.server);
                        if(logEntry == null) {
                            reply.setContentsPlain("Last logout data not present");
                            reply.finalize_context = true;
                            RedisHandler.sendMessage(reply);
                            return;
                        }

                        reply.setContentsPlain("Last logout: " + DATE_FORMAT.format(logEntry.getTime()));
                        RedisHandler.sendMessage(reply);

                        reply.setContentsPlain("Last IP: " + logEntry.getIp().getHostAddress());
                        reply.finalize_context = true;
                        RedisHandler.sendMessage(reply);
                    }
                }.start();
                return null;
            }

            return reply;
        }

        ChatMessageOut message = makeReply(messageIn);
        for(String server : PlayerHelper.getAllServers()) {
            StringBuilder listTextB = new StringBuilder();
            List<Player> players = PlayerHelper.getOnlinePlayersOnServer(server);
            String listText;
            if(players.isEmpty()) {
                listText = "\u00a7fEmpty";
            } else {
                for (Player ply : PlayerHelper.getOnlinePlayersOnServer(server)) {
                    listTextB.append("\u00a7f, ");
                    listTextB.append(PlayerHelper.getPlayerRankTagRaw(ply.getUniqueId()));
                    listTextB.append(ply.getName());
                }
                listText = "\u00a7f" + listTextB.substring(4);
            }
            message.setContents(
                    LIST_FORMAT,
                    new String[]{
                            server, listText
                    }
            );
            RedisHandler.sendMessage(message);
        }

        return makeBlank(messageIn);
    }
}
