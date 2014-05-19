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
