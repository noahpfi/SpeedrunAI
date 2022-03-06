package com.swirb.speedrunai.utils;

import com.swirb.speedrunai.client.Client;
import com.swirb.speedrunai.main.SpeedrunAI;
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
        SpeedrunAI.getInstance().getLogger().info("Connected a client using address " + address + " with channel " + channel);
    }

    public boolean isConnected() {
        return true;
    }

    public SocketAddress getRawAddress() {
        return this.address;
    }

    public void send(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericFutureListener) {
        if (packet.getClass().equals(ClientboundLoginDisconnectPacket.class) || packet.getClass().equals(ClientboundDisconnectPacket.class)) {
            this.client.controller().shutDown();
            this.client.mouseUtils.stopDestroyingNoMessage();
            this.client.shutDown();
            SpeedrunAI.getInstance().getClientHandler().remove(this.client);
            SpeedrunAI.getInstance().getLogger().info(this.client.getName().getString() + " shut down");
        }
    }
}
