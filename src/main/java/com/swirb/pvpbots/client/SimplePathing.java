package com.swirb.pvpbots.client;

import com.swirb.pvpbots.client.utils.Debug;
import com.swirb.pvpbots.client.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;

import java.util.*;

public class SimplePathing {

    private final Client client;
    private double targetF;

    private final Set<PathN> open;
    private final Set<PathN> closed;

    private PathN start;
    private BlockPos end;
    private BlockPos targetPos;

    private double dx;
    private double radius;

    //TODO make it prefer a direction when faced with 2 ways and or radius too small (update every pointer node leading to current best to be a bit cheaper?)

    public SimplePathing(Client client) {
        this.client = client;
        this.open = new HashSet<>();
        this.closed = new HashSet<>();
        this.start = new PathN(this.client.blockPosition());
        this.dx = 2.5D; // causes fewer nodes to be expanded, higher number will make it go through walls more often (2.5 seemed like a sweetspot)
        this.radius = 10.0D; // larger radius means more complex mazes (not necessarily, can only go to farthest in sight anyway) (default 10)
        this.open.add(this.start);
    }

    public void tick(BlockPos target, boolean releaseKeys) {
        if (releaseKeys) this.releaseAll();
        if (target == null) {
            return;
        }
        this.start = new PathN(this.client.blockPosition());
        this.end = target;
        if (this.client.actualBlockPos().equals(this.end)) {
            return;
        }
        this.targetPos = null;
        this.targetF = Double.MAX_VALUE;
        this.calculate();
    }

    // lightweight version of custom lazy-theta* (it doesn't have to recreate the path at cost of a lower chance of picking the right way)
    private void calculate() {
        long stamp = System.currentTimeMillis();
        while (System.currentTimeMillis() - stamp <= 30) {
            PathN c = this.low();
            if (c == null) {
                this.client.logger().info("path failed");
                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                return;
            }
            if (!this.advSight(c.pointer().blockPos(), c.blockPos())) {
                PathN pointer = this.bestNearby(c);
                if (pointer == null) {
                    this.client.logger().info("path failed");
                    this.open.clear();
                    this.open.add(this.start);
                    this.closed.clear();
                    return;
                }
                pointer.setPointer(c.pointer());
                c.setPointer(pointer);
            }
            this.open.remove(c);
            this.closed.add(c);
            Debug.visualizeBlockPosition(this.client.level, c.blockPos(), Color.WHITE, 1.0F);
            if (/*c.blockPos().equals(this.end) ||*/ this.visible(c.blockPos())) {

                Debug.visualizeBlockPosition(this.client.level, c.blockPos(), Color.RED, 2.0F);
                this.client.input.SPRINT = true;
                this.client.input.SPACE = true;
                this.client.input.W = true;
                this.client.lookAt(c.blockPos());
                this.targetF = c.F();
                this.targetPos = c.blockPos();

                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                return;
            }
            for (PathN adjacent : this.adjacents(c)) {
                PathN inOpen = this.nodeIn(adjacent, this.open);
                PathN inClosed = this.nodeIn(adjacent, this.closed);
                if (inOpen != null && adjacent.G() < inOpen.G()) {
                    inOpen.setG(adjacent.G());
                    inOpen.setPointer(adjacent.pointer());
                }
                if (inClosed != null && adjacent.G() < inClosed.G()) {
                    this.closed.remove(inClosed);
                }
                if (inOpen == null && inClosed == null) {
                    this.open.add(adjacent);
                }
            }
        }
    }

    private Set<PathN> adjacents(PathN n) {
        Set<PathN> s = new HashSet<>();
        int x = n.blockPos().getX() - 1;
        int y = n.blockPos().getY() - 1;
        int z = n.blockPos().getZ() - 1;
        for (int i = x; i < x + 3; i++) {
            for (int j = y; j < y + 3; j++) {
                for (int k = z; k < z + 3; k++) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    if (!blockPos.equals(n.blockPos()) && MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false) < Math.pow(this.radius + 1, 2)) {
                        s.add(new PathN(blockPos, n.pointer(), n.pointer().G() + this.cost(n.pointer().blockPos(), blockPos), this.dx * MathUtils.distanceSquared(blockPos, this.end, true, false)));
                    }
                }
            }
        }
        return s;
    }

    public PathN bestNearby(PathN n) {
        PathN best = null;
        for (PathN adjacent : this.adjacents(n)) {
            adjacent.setG(n.G() + this.cost(n.blockPos(), adjacent.blockPos()));
            if (best == null || adjacent.G() < best.G()) {
                best = adjacent;
            }
        }
        return best == null ? null : new PathN(best.blockPos());
    }

    private double cost(BlockPos from, BlockPos to) {
        double cost = 0.0D;
        // diagonal wall
        if (this.diagonalWall(from, to) || this.diagonalWall(from.above(), to.above()) || this.diagonalWall(from.above(), to) || this.diagonalWall(from, to.above())) {
            cost += 100.0D;
        }
        // bottom part of node blocked
        if (!this.passable(to)) {
            cost += this.costToBreak(to);
        }
        // top part of node blocked
        if (!this.passable(to.above())) {
            cost += this.costToBreak(to);
        }
        // node is floating
        if ((this.passable(to.below()) && this.client.level.getBlockState(to.below()).getMaterial() != Material.WATER) && ((from.getX() != to.getX() || from.getZ() != to.getZ()) || from.getY() < to.getY())) {
            if (from.getY() < to.getY()) {
                cost += 2.0D;
            }
            else cost += 10.0D;
        }
        // node is damaging
        if (!this.client.level.getEntities(this.client, new AABB(to), entity -> entity.getType().equals(EntityType.AREA_EFFECT_CLOUD) && ((AreaEffectCloud) entity).effects.stream().anyMatch(effect -> !effect.getEffect().isBeneficial())).isEmpty()
                || this.client.level.getBlockState(to.below()).getMaterial() == Material.LAVA) {
            cost += 100.0D;
        }
        // node is blocked by entity
        if (!this.client.level.getEntities(this.client, new AABB(to), EntitySelector.CAN_BE_COLLIDED_WITH).isEmpty()) {
            cost += 100.0D;
        }
        return MathUtils.distanceSquared(from, to, true, false) + cost;
    }

    private double costToBreak(BlockPos blockPos) {
        double hardness = this.client.level.getBlockState(blockPos).destroySpeed;
        return (12.0D / this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(blockPos)).getDestroySpeed(this.client.level.getBlockState(blockPos))) + (hardness * 0.5D);
    }

    private PathN low() {
        PathN low = null;
        for (PathN n : this.open) {
            if (low == null || n.F() < low.F() || (n.F() == low.F() && n.H() < low.H())) {
                low = n;
            }
        }
        return low;
    }

    private PathN nodeIn(PathN n, Set<PathN> s) {
        for (PathN n1 : s) {
            if (n1.equals(n)) {
                return n1;
            }
        }
        return null;
    }

    private boolean inRadius(BlockPos blockPos) {
        return blockPos != null && MathUtils.distanceSquared(this.client.actualBlockPos(), blockPos, false, false) < this.radius * this.radius;
    }

    private boolean visible(BlockPos blockPos) {
        return (this.sight(this.client.blockPosition().getX(), this.client.actualHeight(), this.client.blockPosition().getZ(), blockPos)
                || (this.inRadius(blockPos) && !this.sight(this.client.blockPosition().getX(), this.client.actualHeight(), this.client.blockPosition().getZ(), this.targetPos)))
                && !blockPos.equals(this.client.actualBlockPos())
                && !blockPos.equals(this.client.actualBlockPos().above());
    }

    private boolean notVisible(BlockPos blockPos) {
        double dist = MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false);
        return dist >= this.radius * this.radius || (dist < this.radius * this.radius && !this.sight(this.client.actualBlockPos(), blockPos)) && !blockPos.equals(this.end);
    }

    private boolean sight(double x, double y, double z, BlockPos to) {
        if (to == null) {
            return false;
        }
        return this.client.level.clip(new ClipContext(new Vec3(x, y, z), Vec3.atCenterOf(to), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    private boolean sight(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        return this.client.level.clip(new ClipContext(Vec3.atCenterOf(from), Vec3.atCenterOf(to), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    // the advanced check as in Pathing cause mojang raytrace cuts corners lel
    private boolean advSight(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.getY() != to.getY() && (from.getX() != to.getX() || from.getZ() != to.getZ())) {
            return false;
        }
        Vec3 vec3F0 = Vec3.atCenterOf(from).add(0.2D, 0.0D, 0.0D);
        Vec3 vec3F1 = Vec3.atCenterOf(from).add(-0.2D, 0.0D, 0.0D);
        Vec3 vec3F2 = Vec3.atCenterOf(from).add(0.0D, 0.0D, 0.2D);
        Vec3 vec3F3 = Vec3.atCenterOf(from).add(0.0D, 0.0D, -0.2D);
        Vec3 vec3T0 = Vec3.atCenterOf(to).add(0.2D, 0.0D, 0.0D);
        Vec3 vec3T1 = Vec3.atCenterOf(to).add(-0.2D, 0.0D, 0.0D);
        Vec3 vec3T2 = Vec3.atCenterOf(to).add(0.0D, 0.0D, 0.2D);
        Vec3 vec3T3 = Vec3.atCenterOf(to).add(0.0D, 0.0D, -0.2D);
        return this.client.level.clip(new ClipContext(vec3F0, vec3T0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to)
                && this.client.level.clip(new ClipContext(vec3F1, vec3T1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to)
                && this.client.level.clip(new ClipContext(vec3F2, vec3T2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to)
                && this.client.level.clip(new ClipContext(vec3F3, vec3T3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    private boolean diagonalWall(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        if ((from.getX() == to.getX() && from.getZ() == to.getZ()) || from.getY() != to.getY()) {
            return false;
        }
        Vec3 vec3F0 = Vec3.atCenterOf(from).add(0.2D, 0.0D, 0.0D);
        Vec3 vec3F1 = Vec3.atCenterOf(from).add(-0.2D, 0.0D, 0.0D);
        Vec3 vec3F2 = Vec3.atCenterOf(from).add(0.0D, 0.0D, 0.2D);
        Vec3 vec3F3 = Vec3.atCenterOf(from).add(0.0D, 0.0D, -0.2D);
        Vec3 vec3T0 = Vec3.atCenterOf(to).add(0.2D, 0.0D, 0.0D);
        Vec3 vec3T1 = Vec3.atCenterOf(to).add(-0.2D, 0.0D, 0.0D);
        Vec3 vec3T2 = Vec3.atCenterOf(to).add(0.0D, 0.0D, 0.2D);
        Vec3 vec3T3 = Vec3.atCenterOf(to).add(0.0D, 0.0D, -0.2D);
        return (!this.client.level.clip(new ClipContext(vec3F0, vec3T0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to)
                && !this.client.level.clip(new ClipContext(vec3F1, vec3T1, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to))
                || (!this.client.level.clip(new ClipContext(vec3F2, vec3T2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to)
                && !this.client.level.clip(new ClipContext(vec3F3, vec3T3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to));
    }

    private boolean passable(BlockPos blockPos) {
        return this.client.level.getBlockState(blockPos).getCollisionShape(this.client.level, blockPos).isEmpty();
    }

    private void releaseAll() {
        this.client.input.W = false;
        this.client.input.S = false;
        this.client.input.A = false;
        this.client.input.D = false;
        this.client.input.SPRINT = false;
        this.client.input.SHIFT = false;
        this.client.input.SPACE = false;
        this.client.input.LEFT_CLICK = false;
        this.client.input.RIGHT_CLICK = false;
    }
}
