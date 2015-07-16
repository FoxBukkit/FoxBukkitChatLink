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
package com.foxelbox.foxbukkit.chatlink.commands.system;

import com.foxelbox.foxbukkit.chatlink.Messages;
import com.foxelbox.foxbukkit.chatlink.Player;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageIn;
import com.foxelbox.foxbukkit.chatlink.json.ChatMessageOut;
import com.foxelbox.foxbukkit.chatlink.util.CommandException;
import com.foxelbox.foxbukkit.chatlink.util.Utils;
import gnu.trove.map.TCharObjectMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

public abstract class ICommand {
	@Retention(RetentionPolicy.RUNTIME) public @interface Names { String[] value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface Help { String value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface Usage { String value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface Permission { String value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface Disabled { }
	@Retention(RetentionPolicy.RUNTIME) public @interface BooleanFlags { String value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface StringFlags { String value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface NumericFlags { String value(); }
	@Retention(RetentionPolicy.RUNTIME) public @interface NoLogging { }

	public enum FlagType {
		BOOLEAN, STRING, NUMERIC
	}

	private final TCharObjectMap<FlagType> flagTypes = new TCharObjectHashMap<>();

	protected final TCharSet booleanFlags = new TCharHashSet();
	protected final TCharObjectMap<String> stringFlags = new TCharObjectHashMap<>();
	protected final TCharObjectMap<Double> numericFlags = new TCharObjectHashMap<>();

	protected ICommand() {
		this(CommandSystem.instance);
	}

	private ICommand(CommandSystem commandSystem) {
		if (this.getClass().getAnnotation(Disabled.class) != null)
			return;

		for (String name : getNames()) {
			commandSystem.registerCommand(name, this);
		}

		parseFlagsAnnotations();
	}

	private void parseFlagsAnnotations() {
		final BooleanFlags booleanFlagsAnnotation = this.getClass().getAnnotation(BooleanFlags.class);
		if (booleanFlagsAnnotation != null) {
			parseFlagsAnnotation(booleanFlagsAnnotation.value(), FlagType.BOOLEAN);
		}

		final StringFlags stringFlagsAnnotation = this.getClass().getAnnotation(StringFlags.class);
		if (stringFlagsAnnotation != null) {
			parseFlagsAnnotation(stringFlagsAnnotation.value(), FlagType.STRING);
		}

		final NumericFlags numericFlagsAnnotation = this.getClass().getAnnotation(NumericFlags.class);
		if (numericFlagsAnnotation != null) {
			parseFlagsAnnotation(numericFlagsAnnotation.value(), FlagType.NUMERIC);
		}
	}

	private void parseFlagsAnnotation(final String flags, final FlagType flagType) {
		for (int i = 0; i < flags.length(); ++i) {
			flagTypes.put(flags.charAt(i), flagType);
		}
	}

	protected String parseFlags(String argStr) throws CommandException {
		if (argStr.trim().isEmpty()) {
			booleanFlags.clear();
			stringFlags.clear();
			numericFlags.clear();
			return argStr;
		}

		String[] args = argStr.split(" ");

		args = parseFlags(args);

		if (args.length == 0)
			return "";

		StringBuilder sb = new StringBuilder(args[0]);
		for (int i = 1; i < args.length; ++i) {
			sb.append(' ');
			sb.append(args[i]);
		}

		return sb.toString();
	}

	protected String[] parseFlags(String[] args) throws CommandException {
		int nextArg = 0;

		parseFlagsAnnotations();
		booleanFlags.clear();
		stringFlags.clear();
		numericFlags.clear();

		while (nextArg < args.length) {
			// Fetch argument
			String arg = args[nextArg++];

			// Empty argument? (multiple consecutive spaces)
			if (arg.isEmpty())
				continue;

			// No more flags?
			if (arg.charAt(0) != '-' || arg.length() == 1) {
				--nextArg;
				break;
			}

			// Handle flag parsing terminator --
			if (arg.equals("--"))
				break;

			if (!Character.isLetter(arg.charAt(1))) {
				--nextArg;
				break;
			}

			// Go through the flags
			for (int i = 1; i < arg.length(); ++i) {
				char flagName = arg.charAt(i);

				final FlagType flagType = flagTypes.get(flagName);
				if (flagType == null)
					throw new CommandException("Invalid flag '"+flagName+"' specified.");

				switch (flagType) {
				case BOOLEAN:
					booleanFlags.add(flagName);
					break;

				case STRING:
					// Skip empty arguments...
					while (nextArg < args.length && args[nextArg].isEmpty())
						++nextArg;

					if (nextArg >= args.length)
						throw new CommandException("No value specified for "+flagName+" flag.");

					stringFlags.put(flagName, args[nextArg++]);
					break;

				case NUMERIC:
					// Skip empty arguments...
					while (nextArg < args.length && args[nextArg].isEmpty())
						++nextArg;

					if (nextArg >= args.length)
						throw new CommandException("No value specified for "+flagName+" flag.");

					numericFlags.put(flagName, Double.parseDouble(args[nextArg++]));
					break;
				}
			}
		}

		return Arrays.copyOfRange(args, nextArg, args.length);
	}

    public static ChatMessageOut makeReply(ChatMessageIn messageIn) {
        ChatMessageOut message = new ChatMessageOut(messageIn);
        message.to.type = Messages.TargetType.PLAYER;
        message.to.filter = new String[] { message.from.uuid.toString() };
        return message;
    }

	public static ChatMessageOut makeBlank(ChatMessageIn messageIn) {
		ChatMessageOut message = makeReply(messageIn);
		message.type = Messages.MessageType.BLANK;
		return message;
	}

    public static ChatMessageOut makeError(ChatMessageIn messageIn, String error) {
        ChatMessageOut message = makeReply(messageIn);
        message.contents = "<color name=\"dark_red\">[FBCL] " + Utils.XMLEscape(error) + "</color>";
        return message;
    }

    public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String[] args) throws CommandException {
        throw new CommandException("Not implemented");
    }

    public ChatMessageOut run(Player sender, ChatMessageIn messageIn, String formattedName, String argStr) throws CommandException {
        if(argStr != null && !argStr.isEmpty()) {
			return run(sender, messageIn, formattedName, argStr.split(" "));
		} else {
			return run(sender, messageIn, formattedName, new String[0]);
		}
    }


	public boolean canPlayerUseCommand(Player commandSender) {
		final String requiredPermission = getRequiredPermission();
		if (requiredPermission != null)
			return commandSender.hasPermission(requiredPermission);

		return true;
	}


	public String[] getNames() {
		final Names namesAnnotation = this.getClass().getAnnotation(Names.class);
		if (namesAnnotation == null)
			return new String[0];

		return namesAnnotation.value();
	}

	public final String getHelp() {
		final Help helpAnnotation = this.getClass().getAnnotation(Help.class);
		if (helpAnnotation == null)
			return "";

		return helpAnnotation.value();
	}

	public final String getUsage() {
		final Usage usageAnnotation = this.getClass().getAnnotation(Usage.class);
		if (usageAnnotation == null)
			return "";

		return usageAnnotation.value();
	}

	public String getRequiredPermission() {
		final Permission permissionAnnotation = this.getClass().getAnnotation(Permission.class);
		if (permissionAnnotation == null)
			return null;

		return permissionAnnotation.value();
	}
}
