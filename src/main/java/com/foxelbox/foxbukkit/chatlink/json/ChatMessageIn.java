package com.foxelbox.foxbukkit.chatlink.json;

import java.util.UUID;

public class ChatMessageIn {
    public String server;

    public UserInfo from;

    public long timestamp;
    public UUID context;

    public String type;
    public String contents;
}
