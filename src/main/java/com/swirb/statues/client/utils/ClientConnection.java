package com.swirb.statues.client.utils;

import com.swirb.statues.Statues;
import com.swirb.statues.client.Client;
import io.netty.channel.local.LocalServerChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;

import javax.annotation.Nullable;
import java.net.SocketAddress;

public class ClientConnection extends Connection {

    private final Client client;

    public ClientConnection(PacketFlow packetFlow, Client client) {
        super(packetFlow);
        this.client = client;
        this.channel = new LocalServerChannel();
        this.address = ConnectionUtils.randomSocketAddress();
        Statues.getInstance().getLogger().info("Using address " + address + " (channel: " + channel + ")");
    }

    public boolean isConnected() {
        return true;
    }

    public SocketAddress getRawAddress() {
        return this.address;
    }

    public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
        if (packet.getClass().equals(ClientboundLoginDisconnectPacket.class) || packet.getClass().equals(ClientboundDisconnectPacket.class)) {
            this.client.shutDown();
            Statues.getClientHandler().remove(this.client);
            this.client.logger().info("shut down");
        }
    }
}
