package com.swirb.speedrunai.main;

import com.swirb.speedrunai.client.ClientHandler;
import com.swirb.speedrunai.command.Commands;
import com.swirb.speedrunai.command.GameMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class SpeedrunAI extends JavaPlugin {

    private static SpeedrunAI instance;
    private static ClientHandler clientHandler;
    public static Logger LOGGER = LogManager.getLogger("SpeedrunAI");

    @Override
    public void onEnable() {
        instance = this;
        clientHandler = new ClientHandler();
        this.registerEvents();
        this.registerCommands();
        ((CraftServer) this.getServer()).getServer().setUsesAuthentication(true);  // offline mode
    }

    @Override
    public void onDisable() {
        clientHandler.clear();
    }

    private void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerCommands() {
        this.getCommand("bot").setExecutor(new Commands(clientHandler));
        this.getCommand("gamemode").setExecutor(new GameMode());
    }

    public static SpeedrunAI getInstance() {
        return instance;
    }
    public static ClientHandler getClientHandler() {
        return clientHandler;
    }
}
