package com.foxelbox.foxbukkit.chatlink.bans;

import java.sql.Date;

public class LogEntry {
    private String action;
    private Date time;
    private char[] ip;
    private int player;

    public BanPlayer getPlayer() {
        return BanResolver.getUserByID(player);
    }

    public String getAction() {
        return action;
    }

    public Date getTime() {
        return time;
    }

    public char[] getIp() {
        return ip;
    }
}
