package com.swirb.pvpbots.client.utils;

import com.google.common.collect.Lists;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.swirb.pvpbots.client.Client;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MathUtils {

    public static float[] yawPitch(Vec3 vec3) {
        double x = vec3.x;
        double z = vec3.z;
        float[] f = new float[2];
        if (x == 0.0D && z == 0.0D) {
            f[1] = (vec3.y > 0.0D ? -90.0F : 90.0F);
            return f;
        }
        double phi = Math.atan2(-x, z);
        f[0] = (float) Math.toDegrees((phi + 6.283185307179586D) % 6.283185307179586D);
        double xz = Math.sqrt((x * x) + (z * z));
        f[1] = (float) Math.toDegrees(Math.atan(-vec3.y / xz));
        return f;
    }

    public static Vec3 vec3Of(double yaw, double pitch) {
        double xz = Math.cos(Math.toRadians(pitch));
        return new Vec3(-xz * Math.sin(Math.toRadians(yaw)), -Math.sin(Math.toRadians(pitch)), xz * Math.cos(Math.toRadians(yaw)));
    }

    public static Vector convert(Vec3 vec3) {
        return new Vector(vec3.x, vec3.y, vec3.z);
    }

    public static Vec3 convert(Vector vector) {
        return new Vec3(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vec3 midpoint(Vec3 vec3, Vec3 vec31) {
        return vec3.add(vec31.x, vec31.y, vec31.z).scale(0.5D);
    }

    public static Vec3 inverseMidpoint(Vec3 vec3, Vec3 vec31) {
        return vec3.subtract(vec31.x, vec31.y, vec31.z).scale(0.5D);
    }

    public static Vec3 divide(Vec3 vec3, Vec3 vec31) {
        return new Vec3(vec3.x / vec31.x, vec3.y / vec31.y, vec3.z / vec31.z);
    }
    
    public static BlockPos closest(BlockPos to, Set<BlockPos> in, boolean ignoreY) {
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos position : in) {
            double distance = distanceSquared(to, position, false, ignoreY);
            if (distance < closestDistance) {
                closest = position;
                closestDistance = distance;
            }
        }
        return closest;
    }

    public static BlockPos farthest(BlockPos to, Set<BlockPos> in, boolean ignoreY) {
        BlockPos farthest = null;
        double farthestDistance = 0.0D;
        for (BlockPos position : in) {
            double distance = distanceSquared(to, position, false, ignoreY);
            if (distance > farthestDistance) {
                farthest = position;
                farthestDistance = distance;
            }
        }
        return farthest;
    }

    public static double distanceSquared(BlockPos start, BlockPos end, boolean root, boolean ignoreY) {
        double d = NumberConversions.square(end.getX() - start.getX()) + (ignoreY ? 0.0D : NumberConversions.square(end.getY() - start.getY())) + NumberConversions.square(end.getZ() - start.getZ());
        return root ? Math.sqrt(d) : d;
    }

    public static double distanceSquared(double x, double y, double z, double x1, double y1, double z1, boolean root, boolean ignoreY) {
        double d = NumberConversions.square(x1 - x) + (ignoreY ? 0.0D : NumberConversions.square(y1 - y)) + NumberConversions.square(z1 - z);
        return root ? Math.sqrt(d) : d;
    }

    public static double distanceSquared(Vec3 vec3, Vec3 vec31, boolean root, boolean ignoreY) {
        double d = NumberConversions.square(vec31.x - vec3.x) + (ignoreY ? 0.0D : NumberConversions.square(vec31.y - vec3.y)) + NumberConversions.square(vec31.z - vec3.z);
        return root ? Math.sqrt(d) : d;
    }

    public static Set<BlockPos> sphere(BlockPos center, int radius) {
        final Set<BlockPos> positions = new HashSet<>();
        final int centerX = center.getX();
        final int centerY = center.getY();
        final int centerZ = center.getZ();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = centerY - radius; y <= centerY + radius; y++) {
                    double dist = (centerX - x) * (centerX - x) + (centerY - y) * (centerY - y) + (centerZ - z) * (centerZ - z);
                    if (dist < radius * radius) {
                        BlockPos BlockPos = new BlockPos(x, y, z);
                        positions.add(BlockPos);
                    }
                }
            }
        }
        return positions;
    }

    public static Set<BlockPos> cylinder(BlockPos center, int radius, int height) {
        final Set<BlockPos> positions = new HashSet<>();
        final int centerX = center.getX();
        final int centerY = center.getY();
        final int centerZ = center.getZ();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                for (int y = centerY; y < centerY + height; y++) {
                    double dist = (centerX - x) * (centerX - x) + (centerZ - z) * (centerZ - z);
                    if (dist < radius * radius) {
                        BlockPos BlockPos = new BlockPos(x, y, z);
                        positions.add(BlockPos);
                    }
                }
            }
        }
        return positions;
    }

    public static double percentOf(Set<BlockPos> positions, Predicate<BlockPos> predicate) {
        Set<BlockPos> has = new HashSet<>();
        for (BlockPos position : positions) {
            if (predicate.test(position)) {
                has.add(position);
            }
        }
        return 100.0D / positions.size() * has.size();
    }

    public static double max(Vec3 vec3) {
        if (vec3.x > vec3.y && vec3.x > vec3.z) {
            return vec3.x;
        }
        else if (vec3.y > vec3.x && vec3.y > vec3.z) {
            return vec3.x;
        }
        else {
            return vec3.z;
        }
    }

    //TODO for some reason, remote players don't have sideways velocity; calculate it
    //TODO (probably cause the client is sending position packets instead of keyboard input packets)
    public static double[] shootingAngle(Vec3 fromPos, Vec3 fromVel, Vec3 targetPos, Vec3 targetVel, Vec3 shotVel, boolean higherTrajectory) {
        double dx = -MathUtils.distanceSquared(fromPos, targetPos, true, true);
        double dy = targetPos.y() - fromPos.y();
        double drag = 0.99D;
        double v = shotVel.lengthSqr();
        double g = 1.0D / 10.0D;    // normally 1 / 20, to adjust height 1 / 15

        double time = (v * Math.abs(dx)) / 20.0D;
        Vec3 leadTargetPos = targetPos.add(targetVel.add(fromVel).scale(time));

        v = v * Math.pow(drag, time);
        double root = Math.sqrt(Math.pow(v, 4) - (g * ((g * Math.pow(dx, 2)) + (2 * dy * Math.pow(v, 2)))));
        double angle0 = Math.toDegrees(Math.atan((Math.pow(v, 2) + root) / (g * dx)));
        double angle1 = Math.toDegrees(Math.atan((Math.pow(v, 2) - root) / (g * dx)));
        double angleVertical = higherTrajectory ? angle0 : angle1;
        double angleHorizontal = MathUtils.yawPitch(leadTargetPos.subtract(fromPos))[0];

        return new double[] {angleHorizontal, angleVertical};
    }

    public static Vec3 calculateArrowVelocity(Client client) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemstack = client.getItemInHand(hand);
            if (itemstack.getItem() instanceof BowItem) {
                int j = itemstack.getItem().getUseDuration(itemstack) - client.getUseItemRemainingTicks();
                float f = (float)j / 20.0F;
                f = (f * f + f * 2.0F) / 3.0F;
                if (f > 1.0F) {
                    f = 1.0F;
                }
                if ((double)f >= 0.1D) {
                    float f5 = -Mth.sin(client.getYRot() * 0.017453292F) * Mth.cos(client.getXRot() * 0.017453292F);
                    float f6 = -Mth.sin((client.getXRot() + 0.0F) * 0.017453292F);
                    float f7 = Mth.cos(client.getYRot() * 0.017453292F) * Mth.cos(client.getXRot() * 0.017453292F);
                    Vec3 vec3 = client.getDeltaMovement();
                    Vec3 vec32 = new Vec3(f5, f6, f7).normalize().scale((double) f * 3.0D);
                    return new Vec3(vec32.x, vec32.y, vec32.z).add(vec3.x, client.isOnGround() ? 0.0D : vec3.y, vec3.z);
                }
                client.logger().warning("bow charged too little, arrow cannot reach target");
            }
            else if (itemstack.getItem() instanceof CrossbowItem) {
                if (client.getInventory().offhand.get(0).getItem() == Items.FIREWORK_ROCKET) {
                    return Vec3.ZERO;
                }
                List<ItemStack> list = Lists.newArrayList();
                CompoundTag compoundTag = itemstack.getTag();
                if (compoundTag != null && compoundTag.contains("ChargedProjectiles", 9)) {
                    ListTag tagList = compoundTag.getList("ChargedProjectiles", 10);
                    if (tagList != null) {
                        for(int i = 0; i < tagList.size(); ++i) {
                            CompoundTag compoundTag1 = tagList.getCompound(i);
                            list.add(ItemStack.of(compoundTag1));
                        }
                    }
                }
                boolean a = list.stream().anyMatch((itemStack1) -> itemStack1.getItem() == Items.FIREWORK_ROCKET);
                double f = itemstack.getItem() == Items.CROSSBOW && a ? 1.6D : 3.15D;
                float f2 = 0;
                for(int i = 0; i < list.size(); ++i) {
                    ItemStack itemStack = list.get(i);
                    if (!itemStack.isEmpty()) {
                        if (i == 0) {
                            f2 = 0.0F;
                        } else if (i == 1) {
                            f2 = -10.0F;
                        } else if (i == 2) {
                            f2 = 10.0F;
                        }
                    }
                }
                Vec3 vec3 = client.getUpVector(1.0F);
                Quaternion quaternion = new Quaternion(new Vector3f(vec3), f2, true);
                Vec3 vec31 = client.getViewVector(1.0F);
                Vector3f vec3f = new Vector3f(vec31);
                vec3f.transform(quaternion);
                return new Vec3(vec3f.x(), vec3f.y(), vec3f.z()).normalize().scale(f);
            }
            else if (itemstack.getItem() instanceof TridentItem) {
                if (EnchantmentHelper.getRiptide(itemstack) != 0) {
                    return Vec3.ZERO;
                }
                int j = itemstack.getItem().getUseDuration(itemstack) - client.getUseItemRemainingTicks();
                if (j >= 10) {
                    float f5 = -Mth.sin(client.getYRot() * 0.017453292F) * Mth.cos(client.getXRot() * 0.017453292F);
                    float f6 = -Mth.sin((client.getXRot() + 0.0F) * 0.017453292F);
                    float f7 = Mth.cos(client.getYRot() * 0.017453292F) * Mth.cos(client.getXRot() * 0.017453292F);
                    Vec3 vec3 = client.getDeltaMovement();
                    Vec3 vec32 = new Vec3(f5, f6, f7).normalize().scale(2.5D + ((double) EnchantmentHelper.getRiptide(itemstack)) * 0.5D);
                    return new Vec3(vec32.x, vec32.y, vec32.z).add(vec3.x, client.isOnGround() ? 0.0D : vec3.y, vec3.z);
                }
            }
        }
        return Vec3.ZERO;
    }

    public static Direction directionDir(BlockPos blockPosFrom, BlockPos blockPosTo) {
        float yaw = MathUtils.yawPitch(new Vec3(blockPosTo.getX() - blockPosFrom.getX(), 0.0D, blockPosTo.getZ() - blockPosFrom.getZ()))[0];
        return Direction.fromYRot(yaw).getOpposite();
    }

    public static float direction(BlockPos blockPosFrom, BlockPos blockPosTo) {
        float yaw = MathUtils.yawPitch(new Vec3(blockPosTo.getX() - blockPosFrom.getX(), 0.0D, blockPosTo.getZ() - blockPosFrom.getZ()))[0];
        Direction direction = Direction.fromYRot(yaw).getOpposite();
        if (direction == Direction.NORTH) {
            return -135.0F;
        }
        else if (direction == Direction.SOUTH) {
            return 45.0F;
        }
        else if (direction == Direction.EAST) {
            return -45.0F;
        }
        else if (direction == Direction.WEST) {
            return 135.0F;
        }
        return -1.0F;
    }
}
