package com.swirb.pvpbots.client.utils;

import com.mojang.math.Vector3f;
import com.swirb.pvpbots.client.Path;
import com.swirb.pvpbots.client.PathN;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;

public class Debug {

    public static void visualizeLocation(Level level, double x, double y, double z, Color color, float size) {
        level.getMinecraftWorld().sendParticles(null, new DustParticleOptions(new Vector3f(Vec3.fromRGB24(color.asRGB())), size), x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D, false);
    }

    public static void visualizeBlockPosition(Level level, BlockPos blockPos, Color color, float size) {
        visualizeLocation(level, blockPos.getX() + 0.5D, blockPos.getY() + 0.5D,blockPos.getZ() + 0.5D, color, size);
    }

    public static void visualizeVector(Level level, double x, double y, double z, Vec3 vec3, double distance, double steps, Color color, float size) {
        for (int i = 0; i < (distance / steps); i++) {
            Vec3 vec = new Vec3(x, y, z).add(vec3.scale(steps * i));
            visualizeLocation(level, vec.x, vec.y, vec.z, color, size);
        }
    }

    public static void visualizePath(Level level, Path path, Color color, float size) {
        for (PathN n : path.nodes()) {
            Debug.visualizeBlockPosition(level, n.blockPos(), color, size);
        }
    }
}
