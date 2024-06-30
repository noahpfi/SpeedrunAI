package com.swirb.statues.client.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class ConnectionUtils {

    public static InetAddress localAddress() throws UnknownHostException {
        String host = 127 + "." + 0 + "." + 0 + "." + random0To255();
        return InetAddress.getByName(host);
    }

    public static int randomPort() {
        return new Random().nextInt(25565);
    }

    private static int random0To255() {
        return new Random().nextInt(255) + 1;
    }

    public static InetSocketAddress randomSocketAddress() {
        try {
            return new InetSocketAddress(localAddress(), randomPort());
        }
        catch (Exception ignored) {
            return new InetSocketAddress(randomPort());
        }
    }
}
