package com.swirb.statues;

import com.swirb.statues.client.ClientHandler;
import com.swirb.statues.client.command.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Statues extends JavaPlugin {

    private static Statues INSTANCE;
    private static ClientHandler clientHandler;

    public void onEnable() {
        INSTANCE = this;
        clientHandler = new ClientHandler();
        this.registerCommands();
    }

    public void onDisable() {
        clientHandler.clear();
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("statues")).setExecutor(new Commands());
    }

    public static Statues getInstance() {
        return INSTANCE;
    }

    public static ClientHandler getClientHandler() {
        return clientHandler;
    }
}
