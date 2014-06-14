package com.foxelbox.foxbukkit.chatlink;

import java.util.UUID;

public class ConsolePlayer extends Player {
    public ConsolePlayer(String name) {
        super(UUID.nameUUIDFromBytes(name.getBytes()), name);
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public int getLevel() {
        return 9999;
    }

    @Override
    public String getDisplayName() {
        return getName();
    }
}
