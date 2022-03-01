package com.swirb.speedrunai.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ClientProfile extends GameProfile {

    private final String offlineSkin = "ewogICJ0aW1lc3RhbXAiIDogMTY0MDcxMTQzNTYwOCwKICAicHJvZmlsZUlkIiA6ICIxODczZDNhMWVmNmI0YmViYmEyNmRjNTkyYWMzM2ZhNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJub19TbWlseSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mOTNkOTI5OTIyYTk1M2JhOWIwMWFhNzk3ZTc3NTAyZTE4NzZiZTc0ZjNhZTA1ZDI2YTY2N2VjODJjNDdkNWU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
    private final String offlineSignature = "Syv6D7mhcCbA/IyxDDI//bNFWPHf5OLFfodjLONmGvB96GtKQ8wWr1Ea2BK8oFClR12EnD+yJovGDR9KRn3UKeTiTrK2PncCmaH6rGfGkcPCvfl95IzwR0jxHzric3VuiVu+HZYV81GzQ8yTW9sQlS584CLccVSxcxk8dZM1RvosiMSVnHrH5BKqbNg/DoqP8K76UH/4hG7kua6XcN7uGg4w2eAr1UjtfQ3gWtnxDuXRmX1+YG26qxnetrQCEUJOSnSt0VSlB6fj4a87nK7JEitRmgHLqcUYMMusGR6tYkQ6rB6ezku7nLwPBmu0fgE2iAI/sajgcXJtwBH0Sx7xMwJffjAWv6Ai3DebuisVUHPFh15A3dJPir3yDus6DZOmEtJyccnsKIW06ssXKc8ey7etkWBQeUwusWH1yizbNuvBRSRwv4NFZr8Hb44UdLTTH0fVobGcD48BGMI6hQYeCl9NDNXwB5WehNgvEuYfTY/jDi/YdXFlgPSCkTk9jU931+RJQKH2fWHHOomQ89TmiYgwfCL4AHNcpcdxwqNkxOtVIT2CpnMeTgP6ujJ6qPH8X3VKq0D8uAjQHLOJCKHwuBhg4zHlqYMi/a3BhS8g21n0Fm0yff6Z4kKowYBxwlkA9zo0zV2KUghyauywo1bR/Ld/FxgiLd6gp7pLXbTog7Y=";
    private static final Map<String, String[]> skinCache = new HashMap<>();
    private String[] skin;

    public ClientProfile(String name) {
        super(Player.createPlayerUUID(name), name);
        String[] skin = ClientProfile.getSkin(name);
        this.skin = skin;
        this.setSkin(skin);
    }

    public ClientProfile(String name, String skinName) {
        super(Player.createPlayerUUID(name), name);
        String[] skin = ClientProfile.getSkin(skinName);
        this.skin = skin;
        this.setSkin(skin);
    }

    public ClientProfile(String name, String[] skin) {
        super(Player.createPlayerUUID(name), name);
        this.skin = skin;
        this.setSkin(skin);
    }

    public ClientProfile(UUID uuid, String name, String[] skin) {
        super(uuid, name);
        this.skin = skin;
        this.setSkin(skin);
    }

    public ClientProfile(UUID uuid, String name, String skinName) {
        super(uuid, name);
        String[] skin = ClientProfile.getSkin(skinName);
        this.skin = skin;
        this.setSkin(skin);
    }

    public void setSkin(String skinName) {
        this.setSkin(ClientProfile.getSkin(skinName));
    }

    public void setSkin(String[] skin) {
        this.getProperties().put("textures", skin != null ? new Property("textures", skin[0], skin[1]) : new Property("textures", this.offlineSkin, this.offlineSignature));
    }

    private GameProfile createOfflineProfile(String name) {
        return new GameProfile(Player.createPlayerUUID(name), name);
    }

    public static String createRandomName() {
        String name = "";
        int i = new Random().nextInt(17);
        for (int j = 0; j < i; j++) {
            int random = new Random().nextInt(ChatUtils.ALPHABET.length);
            name = name.concat(ChatUtils.ALPHABET[random]);
        }
        return validateName(name);
    }

    public static String validateName(String name) {
        int i = 0;
        for (ServerPlayer player : ((CraftServer) Bukkit.getServer()).getHandle().players) {
            if (player.getName().getString().startsWith(name)) {
                i++;
            }
        }
        return ChatUtils.trimTo16(i == 0 ? name : name.concat(String.valueOf(i)));
    }

    public static String[] getSkin(String name) {
        if (ClientProfile.skinCache.containsKey(name)) {
            return ClientProfile.skinCache.get(name);
        }
        String[] vals = ClientProfile.getSkinFromMojang(name);
        ClientProfile.skinCache.put(name, vals);
        return vals;
    }

    private static String[] getSkinFromMojang(String name) {
        try {
            String uuid = new JsonParser().parse(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream())).getAsJsonObject().get("id").getAsString();
            JsonObject property = new JsonParser().parse(new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false").openStream())).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            return new String[] {property.get("value").getAsString(), property.get("signature").getAsString()};
        } catch (IOException | IllegalStateException ignored) {
        }
        return null;
    }

    public String[] getSkin() {
        return this.skin;
    }
}
