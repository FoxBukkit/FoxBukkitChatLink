/**
 * This file is part of YiffBukkitChatLink.
 *
 * YiffBukkitChatLink is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * YiffBukkitChatLink is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with YiffBukkitChatLink.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.doridian.yiffbukkit.chatlink.util;

import de.doridian.yiffbukkit.chatlink.Player;

import java.util.List;

public class MultiplePlayersFoundException extends PlayerFindException {
	private static final long serialVersionUID = 1L;
	private List<Player> players;

	public MultiplePlayersFoundException(List<Player> players) {
		super("Sorry, multiple players found!");
		this.players = players;
	}

	public List<Player> getPlayers() {
		return players;
	}
}
