package com.swirb.speedrunai;

import com.mojang.math.Vector3f;
import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.path.Node;
import com.swirb.speedrunai.path.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

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

    public static void visualizePath(Path path, Color color, float size) {
        for (Node node : path.nodes()) {
            Debug.visualizeBlockPosition(node.level(), node.position(), color, size);
        }
    }

    public static void visualizePath(Path path, Color color, float size, long time) {
        final int[] i = {0};
        new BukkitRunnable() {
            public void run() {
                i[0]++;
                for (Node node : path.nodes()) {
                    Debug.visualizeBlockPosition(node.level(), node.position(), color, size);
                }
                if (i[0] == time / 5) this.cancel();
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 5);
    }
}
