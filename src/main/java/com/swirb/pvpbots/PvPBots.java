package com.swirb.pvpbots;

import com.swirb.pvpbots.client.ClientHandler;
import com.swirb.pvpbots.client.command.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PvPBots extends JavaPlugin {

    private static PvPBots INSTANCE;
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
        Objects.requireNonNull(this.getCommand("bots")).setExecutor(new Commands(clientHandler));
    }

    public static PvPBots getInstance() {
        return INSTANCE;
    }

    public static ClientHandler getClientHandler() {
        return clientHandler;
    }
}
