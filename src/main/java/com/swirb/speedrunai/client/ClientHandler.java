package com.swirb.speedrunai.client;

import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.utils.ClientProfile;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler {

    private final Map<Integer, Client> idMap = new ConcurrentHashMap<>();
    private final Map<String, Client> nameMap = new ConcurrentHashMap<>();

    public void add(Client client) {
        idMap.put(client.getId(), client);
        nameMap.put(client.name, client);
    }

    public void remove(Client client) {
        idMap.remove(client.getId());
        nameMap.remove(client.name);
    }

    public void clear() {
        if(!idMap.isEmpty()) {
            for (Client client : idMap.values()) {
                this.disconnect(client, "reload / server closed");
                remove(client);
            }
        }
    }

    public Client get(String name) {
        return nameMap.get(name);
    }

    public Client get(Player player) {
        return idMap.get(player.getEntityId());
    }

    public Collection<Client> clients() {
        return idMap.values();
    }

    public Collection<String> names() {
        return nameMap.keySet();
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
            add(client);
        }
        sender.sendMessage("took " + ((System.currentTimeMillis() - stamp) / 1000D) + "s");
    }

    public void respawn(Client client) {
        client.server.getPlayerList().respawn(client, false);
        SpeedrunAI.getInstance().getLogger().info(client.name + " respawned");
    }

    public void disconnect(Client client, String quitMessage) {
        client.connection.disconnect(quitMessage);
    }
}
