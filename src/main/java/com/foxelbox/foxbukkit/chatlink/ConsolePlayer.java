package com.foxelbox.foxbukkit.chatlink;

public class ConsolePlayer extends Player {
    public ConsolePlayer(String name) {
        super(Player.CONSOLE_UUID, name);
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
