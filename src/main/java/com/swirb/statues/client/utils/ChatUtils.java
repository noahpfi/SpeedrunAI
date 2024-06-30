package com.swirb.statues.client.utils;

import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;

public class ChatUtils {

    public static final String[] ALPHABET = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
    public static final String DASH = "=======================================================";
    public static final String DASH_THIN = "---------------------------------------------------";

    public static String trimTo16(String string) {
        if (string.length() > 16) {
            string = string.substring(0, 16);
            for (Entity player : ((CraftServer) Bukkit.getServer()).getHandle().players) {
                if (string.equalsIgnoreCase(player.getName().getString())) {
                    string = ClientProfile.createRandomName();
                }
            }
        }
        return string;
    }
}