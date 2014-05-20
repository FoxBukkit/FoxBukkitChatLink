package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.Player;
import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;
import de.doridian.yiffbukkit.chatlink.util.Utils;

public class ListCommand extends ICommand {
    private static final String LIST_FORMAT = "<color name=\"blue\">%1$s</color><color name=\"white\">: %2$s</color>";

    @Override
    public String[] getNames() {
        return new String[] { "who", "list" };
    }

    @Override
    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        message = makeReply(message);
        for(String server : PlayerHelper.getAllServers()) {
            StringBuilder listTextB = new StringBuilder();
            for(Player ply : PlayerHelper.getOnlinePlayersOnServer(server)) {
                listTextB.append(", ");
                listTextB.append(PlayerHelper.getPlayerRankTagRaw(ply.uuid));
                listTextB.append(ply.name);
            }
            String listText = listTextB.substring(2);
            message.contents = new MessageContents("\u00a79" + server + "\u00a7f: " + listText,
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
