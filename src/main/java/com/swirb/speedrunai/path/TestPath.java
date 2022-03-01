package com.swirb.speedrunai.path;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestPath {

    private final Node start;
    private final Node end;
    private final Set<Node> open;
    private final Set<Node> closed;
    private final Path path;
    
    private Node recreationNode;

    public TestPath(Level level, BlockPos start, BlockPos end) {
        this.start = new Node(level, start, 0.0D, 0.0D);
        this.end = new Node(level, end);
        this.open = new HashSet<>(); //new PriorityQueue<>((o1, o2) -> o1.F() == o2.F() ? Double.compare(o1.H(), o2.H()) : Double.compare(o1.F(), o2.F()));
        this.closed = new HashSet<>();
        this.path = new Path();
    }

    public Set<Node> successors(Node from) {
        Set<Node> positions = new HashSet<>();
        BlockPos start = new BlockPos(from.position().getX() - 1.0D, from.position().getY() - 1.0D, from.position().getZ() - 1.0D);
        for (int i = start.getX(); i < (start.getX() + 3); i++) {
            for (int j = start.getY(); j < (start.getY() + 3); j++) {
                for (int k = start.getZ(); k < (start.getZ() + 3); k++) {
                    BlockPos position = new BlockPos(i, j, k);
                    Node node = new Node(from.level(), position, from);
                    if (!node.equals(from)) {
                        positions.add(node);
                    }
                }
            }
        }
        return positions;
    }

    public Node bestNearby(Node from) {
        double G = Double.MAX_VALUE;
        Node best = null;
        for (Node successor : this.successors(from)) {
            if (from.G() + TestPath.this.stepCost(from, successor) < G) {
                G = from.G() + TestPath.this.stepCost(from, successor);
                best = successor;
            }
        }
        return best;
    }

    public double stepCost(Node from, Node to) {
        double cost = 0.0D;
        if (!to.walkable()) {
            cost =+ 10.0D;
        }
        /* + other variables */
        return MathUtils.distanceSquared(from.position(), to.position(), true, false) + cost;
    }

    public Node nodeIn(Node node, Collection<Node> nodes) {
        for (Node node1 : nodes) {
            if (node1.equals(node)) {
                return node1;
            }
        }
        return null;
    }

    public boolean lineOfSight(Level level, BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = Vec3.atCenterOf(from);
        Vec3 vec31 = Vec3.atCenterOf(to);
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    public void calculate() {

        this.open.add(TestPath.this.start);
        
        long stamp = System.currentTimeMillis();
        new BukkitRunnable() {
            public void run() {
                long stamp1 = System.currentTimeMillis();
                while ((System.currentTimeMillis() - stamp1) <= 30) {
                    Node q = TestPath.this.low(); //TestPath.this.open.poll();

                    if (q == null) {
                        System.out.println("path failed (" + ((System.currentTimeMillis() - stamp) / 1000.0D) + ")");
                        this.cancel();
                        break;
                    }

                    if (q.from() != null && !TestPath.this.lineOfSight(q.level(), q.from().position(), q.position())) {
                        q.setFrom(TestPath.this.bestNearby(q));
                        //q.setG();
                    }

                    if (q.equals(TestPath.this.end)) {
                        TestPath.this.end.setFrom(q.from());
                        System.out.println("path found! (" + ((System.currentTimeMillis() - stamp) / 1000.0D) + ")");
                        TestPath.this.recreate();
                        this.cancel();
                        break;
                    }

                    TestPath.this.open.remove(q);
                    TestPath.this.closed.add(q);
                    System.out.println("current F: " + q.F());
                    System.out.println("current open size: " + TestPath.this.open.size());
                    Debug.visualizeBlockPosition(q.level(), q.position(), Color.NAVY, 2.0F);

                    for (Node successor : TestPath.this.successors(q)) {

                        Node inOpen = TestPath.this.nodeIn(successor, TestPath.this.open);
                        Node inClosed = TestPath.this.nodeIn(successor, TestPath.this.closed);

                        if (q.from() != null/* && TestPath.this.lineOfSight(q.level(), q.from().position(), successor.position())*/) {
                            successor.setG(q.from().G() + TestPath.this.stepCost(q.from(), successor));
                            successor.setH(MathUtils.distanceSquared(successor.position(), TestPath.this.end.position(), true, false));
                            successor.setFrom(q.from());

                            if (inOpen != null && successor.G() < inOpen.G()) {
                                inOpen.setG(successor.G());
                                inOpen.setFrom(successor.from());
                            }
                        }
                        else {
                            successor.setG(q.G() + TestPath.this.stepCost(q, successor));
                            successor.setH(MathUtils.distanceSquared(successor.position(), TestPath.this.end.position(), true, false));

                            if (inOpen != null && successor.G() < inOpen.G()) {
                                inOpen.setG(successor.G());
                                inOpen.setFrom(q);
                            }
                        }
                        if (inClosed != null && successor.G() < inClosed.G()) {
                            TestPath.this.closed.remove(inClosed);
                        }
                        if (inOpen == null && inClosed == null) {
                            TestPath.this.open.add(successor);
                        }
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
    }

    public void rec(Node node) {
        if (node != null && node.from() != null) {
            this.path.add(0, node.from());
            this.rec(node.from());
            return;
        }
        this.path.add(this.end);
        this.path.setFound(true);
        System.out.println("path recreated!");
        System.out.println(path.nodes().size());
        Debug.visualizePath(this.path, Color.RED, 2.0F, 200);
    }

    public void recreate() {
        this.recreationNode = this.end;
        new BukkitRunnable() {
            public void run() {
                long stamp1 = System.currentTimeMillis();
                while ((System.currentTimeMillis() - stamp1) <= 30) {
                    System.out.println("recreating");
                    if (TestPath.this.recreationNode.from() != null) {
                        TestPath.this.recreationNode = TestPath.this.recreationNode.from();
                        path.add(0, TestPath.this.recreationNode);
                    }
                    else {
                        path.add(TestPath.this.end);
                        path.setFound(true);
                        System.out.println("path recreated!");
                        System.out.println(path.nodes().size());
                        Debug.visualizePath(TestPath.this.path, Color.RED, 2.0F, 200);
                        this.cancel();
                        break;
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
    }

    private Node low() {
        Node node = null;
        double F = Double.MAX_VALUE;
        for (Node node1 : this.open) {
            if (node1.F() < F) {
                node = node1;
                F = node1.F();
            }
            else if (node1.F() == F && node != null) {
                if (node1.H() < node.H()) {
                    node = node1;
                }
            }
        }
        return node;
    }
}
