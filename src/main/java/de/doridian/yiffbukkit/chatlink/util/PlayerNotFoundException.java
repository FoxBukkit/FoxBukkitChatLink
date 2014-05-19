package de.doridian.yiffbukkit.chatlink.util;

public class PlayerNotFoundException extends PlayerFindException {
	private static final long serialVersionUID = 1L;

	public PlayerNotFoundException() {
		super("Sorry, no player found!");
	}

	public PlayerNotFoundException(Throwable cause) {
		super("Sorry, no player found!", cause);
	}
}
