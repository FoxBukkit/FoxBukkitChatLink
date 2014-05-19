package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import de.doridian.yiffbukkit.chatlink.util.CommandException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class ICommand {
    public abstract String getName();

    public ChatMessage run(ChatMessage message, String formattedName, String[] args) throws CommandException {
        throw new CommandException("Not implemented");
    }

    public ChatMessage run(ChatMessage message, String formattedName, String argStr) throws CommandException {
        return run(message, formattedName, argStr.split(" "));
    }
}
