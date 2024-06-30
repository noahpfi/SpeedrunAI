package com.swirb.statues.client.utils;

import com.swirb.statues.Statues;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.LogRecord;

public class Logger extends java.util.logging.Logger {

    private final String name;

    public Logger(String name) {
        super(name, null);
        this.setParent(Bukkit.getServer().getLogger());
        this.setLevel(Level.ALL);
        this.name = "[" + Statues.getInstance().getName() + "] [" + name + "] ";
    }

    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.name + logRecord.getMessage());
        super.log(logRecord);
    }
}
