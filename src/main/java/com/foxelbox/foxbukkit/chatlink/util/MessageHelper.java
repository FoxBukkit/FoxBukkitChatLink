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
package com.foxelbox.foxbukkit.chatlink.util;

import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessage;
import com.foxelbox.foxbukkit.chatlink.json.MessageContents;
import com.foxelbox.foxbukkit.chatlink.json.MessageTarget;
import com.foxelbox.foxbukkit.chatlink.json.UserInfo;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.UUID;

public class MessageHelper {
	private static final String PLAYER_FORMAT = "<span onClick=\"suggest_command('/pm %1$s ')\"%3$s>%2$s</span>";

	private static final String FB_DEFAULT_COLOR = "dark_purple";
	private static final String FB_ERROR_COLOR = "dark_red";

	public static final String ONLINE_COLOR = "dark_green";
	public static final String OFFLINE_COLOR = "dark_red";

    public static String escape(String s) {
        s = s.replace("&", "&amp;");
        s = s.replace("\"", "&quot;");
        s = s.replace("'", "&apos;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");

        return s;
    }

	public static String format(Player commandSender) {
		return format(commandSender.getUniqueId(), commandSender, false);
	}

	public static String formatWithTag(Player commandSender) {
		return format(commandSender.getUniqueId(), commandSender, true);
	}

	private static String format(UUID uuid, Player commandSender, boolean withTag) {
		final String onHover;
		String displayName, name;
		if (commandSender == null) {
			commandSender = new Player(uuid);
			onHover = "";
			displayName = commandSender.getName();
			name = commandSender.getName();
		}
		else {
			name = commandSender.getName();
			displayName = PlayerHelper.getPlayerRankTag(uuid) + commandSender.getDisplayName();
			final String playerTag = PlayerHelper.getPlayerTagRaw(uuid, false);
			if (withTag && playerTag != null) {
				displayName = playerTag + " " + displayName;
			}
            final String color = commandSender.isOnline() ? ONLINE_COLOR : OFFLINE_COLOR;
            final String hoverText = String.format("<color name=\"%1$s\">%2$s</color>", color, commandSender.getName());
            onHover = " onHover=\"show_text('" + escape(hoverText) + "')\"";
		}
		return String.format(PLAYER_FORMAT, name, displayName, onHover);
	}

	public static String button(String command, String label, String color, boolean run) {
		final String eventType = run ? "run_command" : "suggest_command";
		return String.format("<color name=\"%3$s\" onClick=\"%4$s('%1$s')\" onHover=\"show_text('%1$s')\">[%2$s]</color>", escape(command), escape(label), escape(color), eventType);
	}

	public static ChatMessage sendServerMessage(String format, String... params) {
		return sendColoredServerMessage(FB_DEFAULT_COLOR, format, params);
	}

	public static ChatMessage sendColoredServerMessage(String color, String format, String... params) {
		return sendColoredServerMessage(color, Predicates.<Player>alwaysTrue(), format, params);
	}

	public static ChatMessage sendServerMessage(Predicate<? super Player> predicate, String format, String... params) {
		return sendColoredServerMessage(FB_DEFAULT_COLOR, predicate, format, params);
	}

	public static ChatMessage sendColoredServerMessage(String color, Predicate<? super Player> predicate, String format, String... params) {
		if (color != null) {
			format = "<color name=\"" + color + "\">[FBCL]</color> " + format;
		}

        ChatMessage result = new ChatMessage("", new UserInfo(null, ""), "");
        result.to = new MessageTarget("all", new String[0]);
        result.contents = new MessageContents(null, format, params);
        return result;
	}

	public static ChatMessage sendMessage(Player commandSender, String format, String... params) {
		return sendMessage(FB_DEFAULT_COLOR, commandSender, format, params);
	}

	public static ChatMessage sendErrorMessage(Player commandSender, String format, String... params) {
		return sendMessage(FB_ERROR_COLOR, commandSender, format, params);
	}

	public static ChatMessage sendMessage(String color, Player commandSender, String format, String... params) {
		if (color != null) {
			format = "<color name=\"" + color + "\">[FBCL]</color> " + format;
		}

        ChatMessage result = new ChatMessage("", new UserInfo(null, ""), "");
        result.to = new MessageTarget("player", new String[] { commandSender.getUniqueId().toString() });
        result.contents = new MessageContents(null, format, params);
        return result;
	}
}
