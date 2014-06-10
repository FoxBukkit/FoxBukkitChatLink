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
import com.foxelbox.foxbukkit.chatlink.json.ChatMessage;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.List;

public class ListCommand extends ICommand {
    private static final String LIST_FORMAT = "<color name=\"dark_purple\">[FBCL]</color> <color name=\"dark_gray\">[%1$s]</color> %2$s";

    @Override
    public String[] getNames() {
        return new String[] { "who", "list" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        message = makeReply(message);
        for(String server : PlayerHelper.getAllServers()) {
            StringBuilder listTextB = new StringBuilder();
            List<Player> players = PlayerHelper.getOnlinePlayersOnServer(server);
            String listText;
            if(players.isEmpty()) {
                listText = "\u00a7fEmpty";
            } else {
                for (Player ply : PlayerHelper.getOnlinePlayersOnServer(server)) {
                    listTextB.append("\u00a7f, ");
                    listTextB.append(PlayerHelper.getPlayerRankTagRaw(ply.uuid));
                    listTextB.append(ply.name);
                }
                listText = "\u00a7f" + listTextB.substring(4);
            }
            message.contents = new MessageContents("\u00a75[FBCL] \u00a78[" + server + "] " + listText,
                    LIST_FORMAT,
                    new String[]{
                            server, listText
                    }
            );
            RedisHandler.sendMessage(message);
        }
        return null;
    }
}
