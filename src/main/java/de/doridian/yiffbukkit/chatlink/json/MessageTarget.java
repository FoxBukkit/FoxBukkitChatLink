package de.doridian.yiffbukkit.chatlink.json;

public class MessageTarget {
    public MessageTarget(String type, String[] filter) {
        this.type = type;
        this.filter = filter;
    }

    public String type;
    public String[] filter;
}
