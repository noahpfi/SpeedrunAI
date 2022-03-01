package com.swirb.speedrunai.client;

import com.swirb.speedrunai.utils.ClientProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClientHandler {

    private final Set<Client> clients;
    private final Logger LOGGER = LogManager.getLogger();

    public ClientHandler() {
        this.clients = ConcurrentHashMap.newKeySet();
    }

    public void add(Client client) {
        this.clients.add(client);
    }

    public void remove(Client client) {
        this.clients.remove(client);
    }

    public void clear() {
        if (!this.clients.isEmpty()) {
            for (Client client : clients) {
                this.disconnect(client, "reload / server closed");
            }
            this.clients.clear();
        }
    }

    public Client get(String name) {
        for (Client client : this.clients) {
            if (name.equalsIgnoreCase(client.getName().getString())) {
                return client;
            }
        }
        return null;
    }

    public Client get(Player player) {
        for (Client client : this.clients) {
            if (client.getId() == ((CraftPlayer) player).getHandle().getId()) {
                return client;
            }
        }
        return null;
    }

    public Set<Client> clients() {
        return this.clients;
    }

    public List<String> names() {
        return this.clients.stream().map(client -> client.getName().getString()).collect(Collectors.toList());
    }

    public void createClients(Player sender, String name, int amount) {
        this.createClients(sender, name, name, amount);
    }

    public void createClients(Player sender, String name, String skinName, int amount) {
        this.createClients(sender, name, ClientProfile.getSkin(skinName), amount);
    }

    public void createClients(Player sender, String name, String[] skin, int amount) {
        long stamp = System.currentTimeMillis();
        amount = Math.max(amount, 1);
        for (int i = 0; i < amount; i++) {
            Client client = new Client(((CraftPlayer) sender).getHandle().level, name, skin);
            this.clients.add(client);
        }
        sender.sendMessage("took " + ((System.currentTimeMillis() - stamp) / 1000D) + "s");
    }

    public void respawn(Client client) {
        client.server.getPlayerList().respawn(client, false);
        this.LOGGER.info("[{}] respawned", client.getName().getString());
    }

    public void disconnect(Client client, String quitMessage) {
        client.connection.disconnect(quitMessage);
    }
}
