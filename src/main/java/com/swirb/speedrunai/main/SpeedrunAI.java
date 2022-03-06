package com.swirb.speedrunai.main;

import com.swirb.speedrunai.client.ClientHandler;
import com.swirb.speedrunai.command.Commands;
import com.swirb.speedrunai.command.GameMode;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.plugin.java.JavaPlugin;

public class SpeedrunAI extends JavaPlugin {

    private static SpeedrunAI INSTANCE;

    public static SpeedrunAI getInstance() {
        return INSTANCE;
    }

    private ClientHandler clientHandler;

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        clientHandler = new ClientHandler();
        registerCommands();
        ((CraftServer) this.getServer()).getServer().setUsesAuthentication(true);  //TODO: Move to later in lifecycle
    }

    @Override
    public void onDisable() {
        clientHandler.clear();
    }

    private void registerCommands() {
        getCommand("speedrunai").setExecutor(new Commands(clientHandler));
        getCommand("gamemode").setExecutor(new GameMode());
    }

}
