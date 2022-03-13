package com.swirb.speedrunai.client;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.StarN;
import com.swirb.speedrunai.utils.InventoryUtils;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;

import java.util.HashSet;
import java.util.Set;

public class SmrtThetaController {

    private final Client client;
    private boolean active;
    private double targetF;
    private BlockPos lastTarget;

    private final Set<StarN> open;
    private final Set<StarN> closed;

    private StarN start;
    private BlockPos end;

    private double dx;
    private double radius;

    public SmrtThetaController(Client client) {
        this.client = client;
        this.active = false;
        this.open = new HashSet<>();
        this.closed = new HashSet<>();
        this.start = new StarN(this.client.blockPosition());
        this.dx = 2.5D; // causes fewer nodes to be expanded, higher number will make it go through walls more often (2.5 seemed like a sweetspot)
        this.radius = 10.D; // larger radius means more complex mazes (not necessarily, can only go to farthest in sight anyway) (default 10)
        this.open.add(this.start);
    }

    public void tick() {
        Entity entity = this.client.nearest(100, 100, 100, entity1 -> entity1 instanceof Player && !(entity1 instanceof Client));
        this.releaseAll();
        if (!this.active || entity == null) {
            return;
        }
        this.start = new StarN(this.client.blockPosition());
        this.end = entity.blockPosition();
        if (this.client.blockPosition().equals(this.end) || this.client.eyeBlockPosition().equals(this.end)) {
            return;
        }

        boolean newTarget = true;
        BlockPos front = this.client.front();
        if (
                this.lastTarget != null
                && this.lastTarget.getY() > this.client.getY() + 1  //TODO account for jumping
                && ((!this.passable(front.above()) || (!this.passable(front) && (!this.passable(front.above().above()) || !this.passable(this.client.eyeBlockPosition().above()))))
                || (this.client.blockPosition().getX() == this.lastTarget.getX() && this.client.blockPosition().getZ() == this.lastTarget.getZ()))
                && this.client.inventoryUtils.get(item -> item instanceof BlockItem) != null
        ) {
            this.client.inventoryUtils.swap(this.client.inventoryUtils.get(item -> item instanceof BlockItem), InventoryUtils.Section.ITEMS, 0);
            this.client.look(this.client.getYRot(), 90.0F);
            this.client.input.RIGHT_CLICK = true;
            if (!this.passable(this.client.blockPosition().below())) {
                this.client.input.SHIFT = true;
            }
            this.client.input.SPACE = true;
            newTarget = false;
        }
        else if (!this.passable(front.above()) || (!this.passable(front) && (!this.passable(front.above().above()) || !this.passable(this.client.eyeBlockPosition().above())))) {
            this.releaseAll();
            front = !this.passable(front.above()) ? front.above() : front;
            this.client.lookAt(front);
            this.client.inventoryUtils.swap(this.client.inventoryUtils.bestToolFor(this.client.level.getBlockState(front)), InventoryUtils.Section.ITEMS, 0);
            this.client.input.LEFT_CLICK = true;
            newTarget = false;
        }
        this.targetF = Double.MAX_VALUE;
        this.calculate(newTarget);
    }

    private void calculate(boolean newTarget) {
        long stamp = System.currentTimeMillis();
        while (System.currentTimeMillis() - stamp <= 30) {
            StarN c = this.low();
            if (c == null) {
                this.client.logger().info("path failed");
                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                return;
            }
            if (!this.sight(c.pointer().blockPos(), c.blockPos())) {
                StarN pointer = this.bestNearby(c);
                pointer.setPointer(c.pointer());
                c.setPointer(pointer);
            }
            this.open.remove(c);
            this.closed.add(c);
            Debug.visualizeBlockPosition(this.client.level, c.blockPos(), Color.WHITE, 1.0F);
            BlockPos target = this.nextAdjVisible(c.blockPos());
            if (c.blockPos().equals(this.end) || (this.notVisible(c.blockPos()) && target != null && c.F() < this.targetF)) {
                target = c.blockPos().equals(this.end) ? this.end : target;
                if (newTarget) {
                    Debug.visualizeBlockPosition(this.client.level, c.blockPos(), Color.RED, 2.0F);
                    this.client.lookAt(target);
                    this.client.input.W = true;
                    this.client.input.SPRINT = true;
                    this.client.input.SPACE = true;
                    this.lastTarget = target;
                    this.targetF = c.F();
                }
                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                return;
            }
            for (StarN adjacent : this.adjacents(c)) {
                StarN inOpen = this.nodeIn(adjacent, this.open);
                StarN inClosed = this.nodeIn(adjacent, this.closed);
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

    private Set<StarN> adjacents(StarN n) {
        Set<StarN> s = new HashSet<>();
        int x = n.blockPos().getX() - 1;
        int y = n.blockPos().getY() - 1;
        int z = n.blockPos().getZ() - 1;
        for (int i = x; i < x + 3; i++) {
            for (int j = y; j < y + 3; j++) {
                for (int k = z; k < z + 3; k++) {
                    BlockPos blockPos = new BlockPos(i, j, k);
                    if (!blockPos.equals(n.blockPos()) && MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false) < Math.pow(this.radius + 1, 2)) {
                        s.add(new StarN(blockPos, n.pointer(), n.pointer().G() + this.cost(n.pointer().blockPos(), blockPos), this.dx * MathUtils.distanceSquared(blockPos, this.end, true, false)));
                    }
                }
            }
        }
        return s;
    }

    public StarN bestNearby(StarN n) {
        StarN best = null;
        for (StarN adjacent : this.adjacents(n)) {
            adjacent.setG(n.G() + this.cost(n.blockPos(), adjacent.blockPos()));
            if (best == null || adjacent.G() < best.G()) {
                best = adjacent;
            }
        }
        return best == null ? null : new StarN(best.blockPos());
    }

    public BlockPos nextAdjVisible(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            BlockPos relative = blockPos.relative(direction);
            if (this.client.canSee(relative) && this.passable(relative)) {
                return relative;
            }
        }
        return null;
    }

    private double cost(BlockPos from, BlockPos to) {
        double cost = 0.0D;
        // bottom part of node blocked
        if (!this.passable(to)) {
            cost += 10.0D;
        }
        // top part of node blocked
        if (!this.passable(to.above())) {
            cost += 10.0D;
        }
        // node is floating
        if (this.passable(to.below()) && (from.getX() != to.getX() && from.getZ() != to.getZ()) || from.getY() < to.getY()) {
            cost += 10.0D;
        }
        return MathUtils.distanceSquared(from, to, true, false) + cost;
    }

    private StarN low() {
        StarN low = null;
        for (StarN n : this.open) {
            if (low == null || n.F() < low.F() || (n.F() == low.F() && n.H() < low.H())) {
                low = n;
            }
        }
        return low;
    }

    private StarN nodeIn(StarN n, Set<StarN> s) {
        for (StarN n1 : s) {
            if (n1.equals(n)) {
                return n1;
            }
        }
        return null;
    }

    private boolean notVisible(BlockPos blockPos) {
        double dist = MathUtils.distanceSquared(this.client.eyeBlockPosition(), blockPos, false, false);
        return dist >= this.radius * this.radius || (dist < this.radius * this.radius && !this.client.canSee(blockPos)) && !blockPos.equals(this.end);
    }

    private boolean sight(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf(from);
        Vec3 vec31 = Vec3.atCenterOf(to);
        return this.client.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
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

    public void startup() {
        this.active = true;
    }

    public void shutDown() {
        this.releaseAll();
        this.active = false;
    }
}
