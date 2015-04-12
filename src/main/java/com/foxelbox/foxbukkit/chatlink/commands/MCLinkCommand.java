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
import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@ICommand.Names("mclink")
@ICommand.Help("Allows you to link your Minecraft account to your forums account")
@ICommand.Usage("")
@ICommand.Permission("foxbukkit.mclink")
public class MCLinkCommand extends ICommand {
    @Override
    public ChatMessageOut run(final ChatMessageIn messageIn, String formattedName, String argStr) throws CommandException {
        final ChatMessageOut message = makeReply(messageIn);
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(Main.configuration.getValue("mclink-url", "http://foxelbox.com/mclink_int.php?scode=SOMECODE&uuid=") + Utils.URLEncode(messageIn.from.uuid.toString()));
                    URLConnection conn = url.openConnection();
                    System.setProperty("http.agent", "");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.100 Safari/534.30");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(20000);

                    final String link = new BufferedReader(new InputStreamReader(conn.getInputStream())).readLine();

                    message.contents = "\u00a75[FBCL]\u00a7f Go here to complete: " + link;
					message.finalize_context = true;
                    RedisHandler.sendMessage(message);
                } catch(Exception e) {
                    RedisHandler.sendMessage(makeError(messageIn, "Please try again later"));
                }
            }
        }.start();
        return null;
    }
}
