package de.doridian.yiffbukkit.chatlink.json;

public class ChatMessage {
    public ChatMessage(String server, UserInfo from, String plain) {
        this.server = server;
        this.from = from;
        this.to = new MessageTarget("all", null);
        this.contents = new MessageContents(plain);
    }

    public String server;
    public UserInfo from;
    public MessageTarget to;
    public MessageContents contents;
}
