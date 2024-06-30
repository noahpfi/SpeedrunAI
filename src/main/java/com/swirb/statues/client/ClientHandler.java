package com.swirb.statues.client;

import com.swirb.statues.Statues;
import com.swirb.statues.client.utils.ClientProfile;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.craftbukkit.v1_18_R2.block.CraftCommandBlock;
import org.bukkit.craftbukkit.v1_18_R2.command.CraftBlockCommandSender;
import org.bukkit.craftbukkit.v1_18_R2.command.CraftConsoleCommandSender;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler {

    private final Map<Integer, Client> clients = new ConcurrentHashMap<>();

    public void add(Client client) {
        clients.put(client.getId(), client);
    }

    public void remove(Client client) {
        clients.remove(client.getId());
    }

    public void clear() {
        for (Client client : this.clients.values()) {
            this.disconnect(client, "reload / server closed");
            this.remove(client);
        }
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
            this.add(client);
        }
        sender.sendMessage("took " + ((System.currentTimeMillis() - stamp) / 1000D) + "s");
    }

    public void createClientsCommandBlock(CraftBlockCommandSender sender, String name, String skinName, int amount) {
        long stamp = System.currentTimeMillis();
        amount = Math.max(amount, 1);
        for (int i = 0; i < amount; i++) {
            Client client = new Client(sender.getWrapper().getLevel(), name, ClientProfile.getSkin(skinName));
            this.add(client);
        }
        sender.sendMessage("took " + ((System.currentTimeMillis() - stamp) / 1000D) + "s");
    }

    public void respawn(Client client) {
        client.server.getPlayerList().respawn(client, false);
        Statues.getInstance().getLogger().info(client.name + " respawned");
    }

    public void disconnect(Client client, String quitMessage) {
        client.connection.disconnect(quitMessage);
    }

    public Collection<Client> clients() {
        return clients.values();
    }

    public Client get(String name) {
        for (Client client : this.clients.values()) {
            if (client.name.equalsIgnoreCase(name)) {
                return client;
            }
        }
        return null;
    }
}
