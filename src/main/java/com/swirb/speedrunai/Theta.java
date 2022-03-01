package com.swirb.speedrunai;

import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.path.Node;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Color;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Theta {

    private final ServerPlayer client;
    private boolean active;

    private Node start;
    private Node end;
    private final Set<Node> open;
    private final Set<Node> closed;
    private Set<BlockPos> view;

    private BlockPos currentTargetPos;
    private int radius = 8;
    private double hx = 1.0D;

    public Theta(ServerPlayer client) {
        this.client = client;
        this.active = false;

        this.start = new Node(this.client.level, this.client.blockPosition(), new Node(this.client.level, this.client.blockPosition()));
        this.open = new HashSet<>(); //new PriorityQueue<>((o1, o2) -> o1.F() == o2.F() ? Double.compare(o1.H(), o2.H()) : Double.compare(o1.F(), o2.F()));
        this.closed = new HashSet<>();

        this.view = new HashSet<>();

        this.open.add(this.start);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SpeedrunAI.getInstance(), this::tick, 0, 0);
    }

    public void tick() {
        this.start.setBlockPos(this.client.blockPosition());
        BlockPos blockPos = this.client.blockPosition().relative(this.client.getDirection(), 15);
        int height = this.client.getLevel().getChunk(blockPos.getX() >> 4, blockPos.getZ() >> 4).getHeight(Heightmap.Types.MOTION_BLOCKING, blockPos.getX(), blockPos.getZ());
        this.end = new Node(this.client.level, this.client.blockPosition().relative(this.client.getDirection(), 15).atY(height));

        if (this.currentTargetPos != null) {
            Debug.visualizeBlockPosition(this.client.level, this.currentTargetPos, Color.RED, 2.0F);
            if (this.client.blockPosition().equals(this.currentTargetPos)) {
                this.currentTargetPos = null;
            }
        }

        if (this.calculate()) {
            this.open.clear();
            this.open.add(this.start);
            this.closed.clear();
        }
    }

    public boolean calculate() {
        long stamp1 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - stamp1) <= 30 && !this.open.isEmpty()) {
            Node c = this.low();

            if (!this.lineOfSight(c.level(), c.from().position(), c.position())) {
                c.setFrom(this.bestNearby(c));
            }

            this.open.remove(c);
            this.closed.add(c);
            System.out.println("current F: " + c.F() + " | " + "current open size: " + this.open.size());
            Debug.visualizeBlockPosition(c.level(), c.position(), Color.WHITE, 1.0F);

            c.setVisible(MathUtils.distanceSquared(this.client.position(), new Vec3(c.position().getX(), c.position().getY(), c.position().getZ()), false, false) < Math.pow(this.radius, 2) && this.canSee(c.position()) && this.isPassable(c.position()));
            if (!c.visible() || c.equals(this.end)) {
                this.currentTargetPos = c.position();
                return true;
            }

            for (Node successor : this.successors(c)) {
                Node inOpen = this.nodeIn(successor, this.open);
                Node inClosed = this.nodeIn(successor, this.closed);
                successor.setG(c.from().G() + this.stepCost(c.from(), successor));
                successor.setH(this.h(successor, this.end));
                successor.setFrom(c.from());
                if (inOpen != null && successor.G() < inOpen.G()) {
                    inOpen.setG(successor.G());
                    inOpen.setFrom(c.from());
                }
                if (inClosed != null && successor.G() < inClosed.G()) {
                    this.closed.remove(inClosed);
                }
                if (inOpen == null && inClosed == null) {
                    this.open.add(successor);
                }
            }
        }
        System.out.println("path failed");
        return false;
    }

    public Set<Node> successors(Node from) {
        Set<Node> positions = new HashSet<>();
        BlockPos start = new BlockPos(from.position().getX() - 1.0D, from.position().getY() - 1.0D, from.position().getZ() - 1.0D);
        for (int i = start.getX(); i < (start.getX() + 3); i++) {
            for (int j = start.getY(); j < (start.getY() + 3); j++) {
                for (int k = start.getZ(); k < (start.getZ() + 3); k++) {
                    BlockPos position = new BlockPos(i, j, k);
                    Node node = new Node(from.level(), position, from);
                    if (!position.equals(from.position())) {
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
            if (from.G() + this.stepCost(from, successor) < G) {
                G = from.G() + this.stepCost(from, successor);
                best = successor;
            }
        }
        return best;
    }

    public Node nextAdjVisible(Node node) {
        for (Direction direction : Direction.values()) {
            if (this.canSee(node.position().relative(direction))) {
                return new Node(node.level(), node.position().relative(direction));
            }
        }
        return node;
    }

    public Node nextVisible(Node node) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(node.position().offset(1, 0, 1));
        positions.add(node.position().offset(1, 0, -1));
        positions.add(node.position().offset(-1, 0, 1));
        positions.add(node.position().offset(-1, 0, -1));
        for (BlockPos blockPos : positions) {
            if (this.canSee(blockPos)) {
                return new Node(this.client.level, blockPos, node.from());
            }
        }
        return null;
    }

    public Node lowVisible(Node from) {
        double H = Double.MAX_VALUE;
        Node low = from;
        for (Direction direction : Direction.values()) {
            Node n = new Node(from.level(), from.position().relative(direction));
            if (this.canSee(from.position().relative(direction)) && MathUtils.distanceSquared(from.position(), this.client.blockPosition(), false, false) < H) {
                low = n;
                H = MathUtils.distanceSquared(from.position(), this.client.blockPosition(), false, false);
            }
        }
        return low;
    }

    public double h(Node from, Node to) {
        return this.hx * MathUtils.distanceSquared(from.position(), to.position(), true, false);
    }

    public double stepCost(Node from, Node to) {
        double cost = 0.0D;
        if (!to.walkable()) {
            cost =+ 10.0D;
        }
        /* + other variables */
        return MathUtils.distanceSquared(from.position(), to.position(), true, false) + cost;
    }

    private Node low() {
        double F = Double.MAX_VALUE;
        Node low = null;
        for (Node node : this.open) {
            if (node.F() < F) {
                F = node.F();
                low = node;
            }
            else if (node.F() == F && low != null) {
                if (node.H() < low.H()) {
                    low = node;
                }
            }
        }
        return low;
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

    private boolean isPassable(BlockPos position) {
        return this.client.level.getBlockState(position).getCollisionShape(this.client.level, position).isEmpty();
    }

    public boolean canSee(BlockPos blockPos) {
        if (blockPos == null) {
            return false;
        }
        Vec3 vec3 = new Vec3(this.client.getX(), this.client.getEyeY(), this.client.getZ());
        Vec3 vec31 = new Vec3(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
        return this.client.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(blockPos);
    }
}
