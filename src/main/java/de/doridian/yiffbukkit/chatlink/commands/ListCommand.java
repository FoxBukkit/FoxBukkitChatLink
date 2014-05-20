package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.Player;
import de.doridian.yiffbukkit.chatlink.RedisHandler;
import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.json.MessageContents;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import de.doridian.yiffbukkit.chatlink.util.PlayerHelper;

public class ListCommand extends ICommand {
    private static final String LIST_FORMAT = "<color name=\"dark_gray\">[%1$s]</color> %2$s";

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
                listTextB.append("\u00a7f, ");
                listTextB.append(PlayerHelper.getPlayerRankTagRaw(ply.uuid));
                listTextB.append(ply.name);
            }
            String listText = "\u00a7f" + listTextB.substring(4);
            message.contents = new MessageContents("\u00a78[" + server + "] " + listText,
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
