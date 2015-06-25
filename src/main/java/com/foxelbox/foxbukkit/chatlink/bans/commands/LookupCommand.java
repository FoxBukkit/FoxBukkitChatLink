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

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.bans.Ban;
import com.foxelbox.foxbukkit.chatlink.bans.BanResolver;
import com.foxelbox.foxbukkit.chatlink.bans.FishBansResolver;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.PlayerHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ICommand.Names("lookup")
@ICommand.Help("Gets ban and alt information about specified user")
@ICommand.Usage("<name>")
@ICommand.Permission("foxbukkit.bans.lookup")
public class LookupCommand extends ICommand {
	@Override
	public ChatMessageOut run(ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
		final Player commandSender = Player.getPlayerFromMessage(messageIn);

		final Player otherPly = PlayerHelper.matchPlayerSingle(args[0], false);

		final String user = otherPly.getName();
		final UUID uuid = otherPly.getUniqueId() != null ? otherPly.getUniqueId() : null;
		new Thread() {
			public void run() {
				final Ban ban = BanResolver.getBan(user, uuid);
				final String altList = BanResolver.makePossibleAltString(user, uuid);
				final HashMap<String, Integer> fishBans = FishBansResolver.getBanCounts(user);

				final StringBuilder fishBansStr = new StringBuilder(user + " has");
				for (Map.Entry<String, Integer> fishBanEntry : fishBans.entrySet())
					if (fishBanEntry.getKey() != null && fishBanEntry.getValue() != null)
						fishBansStr.append(String.format(" %1$d ban(s) on %2$s,", fishBanEntry.getValue(), fishBanEntry.getKey()));
				fishBansStr.deleteCharAt(fishBansStr.length() - 1);

				if (ban != null) {
					RedisHandler.sendSimpleMessage(commandSender, String.format("Player %1$s IS banned by %2$s for the reason of \"%3$s\"", user, ban.getAdmin().name, ban.getReason()));
				} else {
					RedisHandler.sendSimpleMessage(commandSender, String.format("Player %1$s is NOT banned", user));
				}
				if (altList != null) {
					RedisHandler.sendSimpleMessage(commandSender, altList);
				} else {
					RedisHandler.sendSimpleMessage(commandSender, String.format("No possible alts of %1$s found", user));
				}
				RedisHandler.sendSimpleMessage(commandSender, fishBansStr.toString());
			}
		}.start();

		return makeBlank(messageIn);
	}
}
