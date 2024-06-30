package com.swirb.pvpbots.client;

import com.swirb.pvpbots.client.utils.InventoryUtils;
import com.swirb.pvpbots.client.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class Pathing {

    private final Client client;

    private final Set<PathN> open;
    private final Set<PathN> closed;

    private PathN start;
    private BlockPos end;
    private BlockPos targetPos;

    private long time;
    private boolean recreating;
    private Path path;

    private double dx;
    private double targetRadius;
    private double viewRadius;

    public Pathing(Client client) {
        this.client = client;
        this.open = new HashSet<>();
        this.closed = new HashSet<>();
        this.start = new PathN(this.client.blockPosition());
        this.time = 0;
        this.recreating = false;
        this.dx = 6.5D; // causes fewer nodes to be expanded, higher number will make it go through walls more often (2.5 seemed like a sweetspot)
        this.targetRadius = 20.0D; // larger radius means more complex mazes (not necessarily, can only go to farthest in sight anyway) (default 10)
        this.viewRadius = 5.0D;
        this.open.add(this.start);
    }

    public void tick(BlockPos target, boolean releaseKeys) {
        if (releaseKeys) this.releaseAll();
        if (target == null) {
            return;
        }
        this.start = new PathN(this.client.blockPosition());
        this.end = target;
        if (this.client.blockPosition().equals(this.end) || this.client.eyeBlockPosition().equals(this.end)) {
            return;
        }
        if (!this.recreating) {
            this.path = this.recreate(this.calculate());
        }
        if (this.path != null) {
            this.targetPos = null;
            while (path.iterator().hasNext()) {
                PathN c = path.iterator().current();
                PathN cn = path.iterator().currentNext();
                if ((this.advSight(c.blockPos(), cn.blockPos()) && (c.blockPos().getX() != cn.blockPos().getX() && c.blockPos().getZ() != cn.blockPos().getZ()))
                        || (c.blockPos().getY() <= cn.blockPos().getY()
                        || (c.blockPos().getY() > cn.blockPos().getY() && (c.blockPos().getX() != cn.blockPos().getX() || c.blockPos().getZ() != cn.blockPos().getZ()) && path.nodes().stream().noneMatch(n -> cn.blockPos().below().getY() == n.blockPos().getY() && this.passable(cn.blockPos().below()))))
                ) {
                    //Debug.visualizeVector(this.client.level, c.blockPos().getX() + 0.5D, c.blockPos().getY() + 0.5D, c.blockPos().getZ() + 0.5D,
                            //new Vec3(cn.blockPos().subtract(c.blockPos()).getX(), cn.blockPos().subtract(c.blockPos()).getY(), cn.blockPos().subtract(c.blockPos()).getZ()).normalize(),
                            //MathUtils.distanceSquared(c.blockPos(), cn.blockPos(), true, false), 0.05D, Color.GREEN, 0.25F);
                    if (this.visible(c.blockPos()) && (!this.sight(this.client.blockPosition().getX(), this.client.actualHeight(), this.client.blockPosition().getZ(), this.targetPos) || Math.abs(this.targetPos.getY() - this.client.actualHeight()) > 2.0D)) {
                        this.targetPos = c.blockPos();
                    }
                }
                else {
                    //Debug.visualizeVector(this.client.level, c.blockPos().getX() + 0.5D, c.blockPos().getY() + 0.5D, c.blockPos().getZ() + 0.5D,
                            //new Vec3(cn.blockPos().subtract(c.blockPos()).getX(), cn.blockPos().subtract(c.blockPos()).getY(), cn.blockPos().subtract(c.blockPos()).getZ()).normalize(),
                            //MathUtils.distanceSquared(c.blockPos(), cn.blockPos(), true, false), 0.05D, Color.RED, 0.25F);
                    this.targetPos = cn.blockPos().above().above().above();
                }
                path.iterator().next();
            }
            if (this.targetPos != null) {
                //Debug.visualizeBlockPosition(this.client.level, this.targetPos, Color.PURPLE, 2.0F);
            }
        }

        if (this.targetPos != null && (!this.passable(this.targetPos) || !this.passable(this.targetPos.above())) && this.client.inReach(this.targetPos)) {
            this.client.logger().info("breaking");
            BlockPos breakPos = !this.passable(this.targetPos.above()) ? this.targetPos.above() : this.targetPos;
            this.client.inventoryUtils.swap(this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(breakPos)), InventoryUtils.Section.ITEMS, 0);
            this.client.input.LEFT_CLICK = true;
            this.client.lookAt(breakPos);
            return;
        }

        if (!this.passable(new BlockPos(this.client.getX(), this.client.actualHeight() + 2.0D, this.client.getZ()))) {
            this.client.logger().info("breaking above");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(this.client.eyeBlockPosition().above())), InventoryUtils.Section.ITEMS, 0);
            this.client.controller().center();
            this.client.input.SHIFT = true;
            this.client.input.LEFT_CLICK = true;
            this.client.lookAt(this.client.eyeBlockPosition().above());
            return;
        }

        if (this.targetPos != null
                && this.targetPos.getX() == this.client.blockPosition().getX()
                && this.targetPos.getZ() == this.client.blockPosition().getZ()
                && this.targetPos.getY() > this.client.actualHeight() + 2.0D
                && this.client.inventoryUtils.has(item -> item instanceof BlockItem)
        ) {
            this.releaseAll();
            this.client.logger().info("stacking up");
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item instanceof BlockItem), InventoryUtils.Section.ITEMS, 0);
            this.client.controller().center();
            this.client.look(this.client.getYRot(), 90.0F);
            if (!this.client.controller().isPassable(new BlockPos(this.client.position()).below())) {
                this.client.input.SHIFT = true;
            }
            this.client.input.SPACE = true;
            this.client.input.RIGHT_CLICK = true;
            return;
        }

        if (this.targetPos != null) {
            this.client.input.SPRINT = true;
            this.client.input.SPACE = true;
            this.client.input.W = true;
            if (this.client.isInWater() && !this.client.isSwimming() && this.client.level.getBlockState(new BlockPos(this.client.position()).below(2)).getMaterial() == Material.WATER) {
                this.client.input.SHIFT = true;
                this.client.input.SPACE = false;
            }
            else this.client.input.SHIFT = false;
            this.client.lookAt(this.targetPos);
        }
    }

    // this is standard A* i think i forgor
    private PathN calculate() {
        long stamp = System.currentTimeMillis();
        while (System.currentTimeMillis() - stamp <= 30) {
            this.time += (System.currentTimeMillis() - stamp);
            PathN c = this.low();
            if (c == null) {
                this.client.logger().info("path failed");
                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                this.time = 0;
                return null;
            }
            //Debug.visualizeBlockPosition(this.client.level, c.blockPos(), Color.WHITE, 0.7F);
            this.open.remove(c);
            this.closed.add(c);
            if (c.blockPos().equals(this.end) || this.notVisible(c.blockPos())) {
                this.client.logger().info("path found (" + this.time + "ms)");
                this.time = 0;
                return c;
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
        return null;
    }

    private Path recreate(PathN n) {
        if (n == null) {
            return null;
        }
        Path path = new Path();
        long stamp = System.currentTimeMillis();
        this.recreating = true;
        while (System.currentTimeMillis() - stamp <= 30) {
            if (n.pointer() != null) {
                path.add(n);
                n = n.pointer();
            }
            else {
                this.client.logger().info("path recreated");
                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                this.recreating = false;
                return path;
            }
        }
        return null;
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
                    if (!blockPos.equals(n.blockPos()) && MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false) < Math.pow(this.targetRadius + 1, 2)) {
                        s.add(new PathN(blockPos, n, n.G() + this.cost(n.blockPos(), blockPos), this.dx * MathUtils.distanceSquared(blockPos, this.end, true, false)));
                    }
                }
            }
        }
        return s;
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
        return (12.0D / this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(blockPos)).getDestroySpeed(this.client.level.getBlockState(blockPos))) + (hardness * 0.8D);
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

    private boolean diagonal(BlockPos from, BlockPos to) {
        return MathUtils.distanceSquared(from, to, false, false) > 1.0D;
    }

    private boolean notVisible(BlockPos blockPos) {
        double dist = MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false);
        return dist >= this.targetRadius * this.targetRadius;
    }

    private boolean visible(BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        double dist = MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false);
        return dist < this.viewRadius * this.viewRadius;
    }

    private boolean sight(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf(from);
        Vec3 vec31 = Vec3.atCenterOf(to).subtract(0.0D, 0.1D, 0.0D);
        return this.client.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    private boolean sight(double x, double y, double z, BlockPos to) {
        if (to == null) {
            return false;
        }
        Vec3 vec3 = new Vec3(x, y, z);
        Vec3 vec31 = Vec3.atCenterOf(to);
        return this.client.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    // vanilla raytrace can go through diagonals where it shouldn't on certain axis
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
