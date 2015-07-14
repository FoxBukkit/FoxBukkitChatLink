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
import com.foxelbox.foxbukkit.chatlink.RedisHandler;
import com.foxelbox.foxbukkit.chatlink.commands.system.ICommand;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;

@ICommand.Names("slacklink")
@ICommand.Help("Allows you to link your Minecraft account to your Slack account")
@ICommand.Usage("<Slack username>")
@ICommand.Permission("foxbukkit.slacklink")
public class SlackLinkCommand extends ICommand {
	@Override
	public ChatMessageOut run(Player commandSender, final ChatMessageIn messageIn, String formattedName, final String argStr) throws CommandException {
		if(argStr.trim().equals("")) {
			throw new CommandException("Usage: /slacklink <Slack username>");
		}

		final ChatMessageOut message = makeReply(messageIn);
		new Thread() {
			public void run() {
				try {
					Main.slackHandler.beginLink(argStr.toLowerCase(), messageIn.from);

					message.setContentsPlain("\u00a75[FBCL]\u00a7f Visit Slack to complete your account linking.");
					message.finalize_context = true;
					RedisHandler.sendMessage(message);
				} catch(CommandException e) {
					RedisHandler.sendMessage(makeError(messageIn, e.getMessage()));
				} catch(Exception e) {
					RedisHandler.sendMessage(makeError(messageIn, "Please try again later."));
				}
			}
		}.start();
		return null;
	}
}
