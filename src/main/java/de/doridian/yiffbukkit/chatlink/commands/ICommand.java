package de.doridian.yiffbukkit.chatlink.commands;

import de.doridian.yiffbukkit.chatlink.json.ChatMessage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class ICommand {
    public abstract String getName();

    public ChatMessage run(ChatMessage message, String formattedName, String[] args) {
        throw new RuntimeException("Not implemented");
    }

    public ChatMessage run(ChatMessage message, String formattedName, String argStr) {
        return run(message, formattedName, argStr.split(" "));
    }
}
