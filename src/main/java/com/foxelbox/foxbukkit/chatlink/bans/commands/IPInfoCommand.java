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
package com.foxelbox.foxbukkit.chatlink.bans.commands;

import com.foxelbox.foxbukkit.chatlink.Main;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.bans.BanResolver;
import com.foxelbox.foxbukkit.chatlink.bans.LogEntry;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@ICommand.Names("ipinfo")
@ICommand.Help("Gets IP info")
@ICommand.Usage("<name>")
@ICommand.Permission("foxbukkit.bans.ipinfo")
public class IPInfoCommand extends ICommand {
    static final String SHODAN_API_KEY = Main.configuration.getValue("shodan-api-key", "");

    private static void sendError(ChatMessageIn messageIn) {
        ChatMessageOut reply = makeReply(messageIn);
        reply.finalizeContext = true;
        reply.setContentsPlain("\u00a75[FBCL]\u00a7f IP data not present");
        Main.chatQueueHandler.sendMessage(reply);
    }

    private static void pushQueue(ChatMessageOut reply, HashMap<String,String> ipInfo) {
        for(Map.Entry<String, String> e : ipInfo.entrySet()) {
            reply.setContentsPlain("\u00a75[FBCL]\u00a7f " + e.getKey() + ": " + e.getValue());
            Main.chatQueueHandler.sendMessage(reply);
        }
        ipInfo.clear();
    }

    @Override
    public ChatMessageOut run(final Player commandSender, final ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        final String ip; final Player target;
        if(args[0].equalsIgnoreCase("[IP]")) {
            target = null;
            ip = args[1];
        } else {
            target = PlayerHelper.matchPlayerSingle(args[0], false);
            ip = null;
        }

        new Thread() {
            public void run() {
                final HashMap<String,String> ipInfo = new HashMap<>();

                final InetAddress ipAddress;
                if (target != null) {
                    final LogEntry logEntry = BanResolver.getLatestEntry(target.getName(), target.getUniqueId(), null, messageIn.server);

                    if (logEntry == null) {
                        sendError(messageIn);
                        return;
                    }

                    final String altList = BanResolver.makePossibleAltString(target.getName(), target.getUniqueId());

                    ipInfo.put("Username", target.getName());
                    ipInfo.put("Alts", altList);

                    ipAddress = logEntry.getIp();
                } else if(ip != null) {
                    try {
                        ipAddress = InetAddress.getByName(ip);
                    } catch (UnknownHostException e) {
                        sendError(messageIn);
                        return;
                    }
                } else {
                    sendError(messageIn);
                    return;
                }

                ChatMessageOut reply = makeReply(messageIn);
                reply.finalizeContext = false;
                reply.setContentsPlain("\u00a7d[FBCL]\u00a7f --- START ---");
                Main.chatQueueHandler.sendMessage(reply);

                final String ip = ipAddress.getHostAddress();
                final String host = ipAddress.getCanonicalHostName();

                ipInfo.put("IP", ip);
                ipInfo.put("Host", host);

                pushQueue(reply, ipInfo);

                try {
                    URLConnection conn = new URL("https://api.shodan.io/shodan/host/" + ip + "?minify=True&key=" + SHODAN_API_KEY).openConnection();
                    InputStream is = conn.getInputStream();
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject)parser.parse(new InputStreamReader(is));

                    ipInfo.put("Location", obj.get("country_name") + " (" + obj.get("city") + ")");
                    ipInfo.put("OS", "" + obj.get("os"));

                    ipInfo.put("Open ports", ((JSONArray)obj.get("ports")).toJSONString());

                    ipInfo.put("Shodan", "https://www.shodan.io/host/" + ip);

                    is.close();
                } catch (IOException|ParseException|RuntimeException e) {
                    e.printStackTrace();
                }

                pushQueue(reply, ipInfo);

                reply.finalizeContext = true;
                reply.setContentsPlain("\u00a7d[FBCL]\u00a7f ---- END ----");
                Main.chatQueueHandler.sendMessage(reply);
            }
        }.start();

        return null;
    }
}
