package de.doridian.yiffbukkit.chatlink.util;

public class CommandException extends Exception {
	private static final long serialVersionUID = 1L;
	
	private char color = '5';

	public CommandException(String message) {
		super(message);
	}

	public CommandException(Throwable cause) {
		super(cause);
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandException setColor(char color) {
		this.color = color;
		return this;
	}

	public char getColor() {
		return color;
	}
}
