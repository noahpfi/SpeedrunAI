package com.swirb.speedrunai;

import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Theta {

    private final Level level;

    private final Set<StarN> open;
    private final Set<StarN> closed;

    private final StarN start;
    private final BlockPos end;

    private final PathN path;
    private StarN recreationN;

    private double dx;
    private double radius;

    public Theta(Level level, BlockPos start, BlockPos end) {
        this.level = level;
        this.open = new HashSet<>();
        this.closed = new HashSet<>();
        this.start = new StarN(start);
        this.end = end;
        this.path = new PathN();
        this.dx = 2.5D; // causes fewer nodes to be expanded, higher number will make it go through walls more often (2.5 seemed like a sweetspot)
        this.radius = 10.D;
        this.open.add(this.start);
    }

    public void calculate() {
        long time = System.currentTimeMillis();
        new BukkitRunnable() {
            public void run() {
                long stamp = System.currentTimeMillis();
                while (System.currentTimeMillis() - stamp <= 30) {
                    StarN c = Theta.this.low();
                    if (c == null) {
                        SpeedrunAI.getInstance().getLogger().info("path failed (" + ((System.currentTimeMillis() - time) / 1000.0D) + ")");
                        this.cancel();
                        break;
                    }
                    if (!Theta.this.sight(c.pointer().blockPos(), c.blockPos())) {
                        StarN pointer = Theta.this.bestNearby(c);
                        pointer.setPointer(c.pointer());
                        c.setPointer(pointer);
                    }
                    Theta.this.open.remove(c);
                    Theta.this.closed.add(c);
                    Debug.visualizeBlockPosition(Theta.this.level, c.blockPos(), Color.WHITE, 1.0F);
                    if (c.blockPos().equals(Theta.this.end)) {
                        SpeedrunAI.getInstance().getLogger().info("path found! (" + ((System.currentTimeMillis() - time) / 1000.0D) + ")");
                        Theta.this.recreate(c);
                        this.cancel();
                        break;
                    }
                    for (StarN adjacent : Theta.this.adjacents(c)) {
                        StarN inOpen = Theta.this.nodeIn(adjacent, Theta.this.open);
                        StarN inClosed = Theta.this.nodeIn(adjacent, Theta.this.closed);
                        if (inOpen != null && adjacent.G() < inOpen.G()) {
                            inOpen.setG(adjacent.G());
                            inOpen.setPointer(adjacent.pointer());
                        }
                        if (inClosed != null && adjacent.G() < inClosed.G()) {
                            Theta.this.closed.remove(inClosed);
                        }
                        if (inOpen == null && inClosed == null) {
                            Theta.this.open.add(adjacent);
                        }
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
    }

    private void recreate(StarN n) {
        this.recreationN = n;
        long time = System.currentTimeMillis();
        new BukkitRunnable() {
            public void run() {
                long stamp = System.currentTimeMillis();
                while (System.currentTimeMillis() - stamp <= 30) {
                    if (Theta.this.recreationN.pointer() != null) {
                        Theta.this.path.add(Theta.this.recreationN);
                        Theta.this.recreationN = Theta.this.recreationN.pointer();
                    }
                    else {
                        SpeedrunAI.getInstance().getLogger().info("path recreated (" + ((System.currentTimeMillis() - time) / 1000.0D) + ")");
                        Collections.reverse(Theta.this.path.nodes());
                        Debug.visualizePathN(Theta.this.level, Theta.this.path, Color.RED, 2.0F, 100);
                        this.cancel();
                        break;
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
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
                    if (!blockPos.equals(n.blockPos()) && MathUtils.distanceSquared(this.start.blockPos(), blockPos, false, false) < Math.pow(this.radius + 1, 2)) {
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
        if (this.passable(to.below())/* && (from.getX() != to.getX() && from.getZ() != to.getZ()) || from.getY() < to.getY()*/) {
            cost += 9999999.0D;
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

    private boolean sight(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf(from);
        Vec3 vec31 = Vec3.atCenterOf(to);
        return this.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    private boolean passable(BlockPos blockPos) {
        return this.level.getBlockState(blockPos).getCollisionShape(this.level, blockPos).isEmpty();
    }
}
