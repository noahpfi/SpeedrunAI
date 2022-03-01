package com.swirb.speedrunai.client;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.path.Node;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Color;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SmrtThetaController {

    private final Client client;
    private boolean active;

    private Node start;
    private Node end;
    private final Set<Node> open;
    private final Set<Node> closed;

    private Node currentTargetPos;
    private int radius = 8;
    private double hx = 1.0D;

    //TODO theta spreading more on the wrong side of a wall causing edge node being targeted problem: fix: up radius (problem still exists on larger scale)
    //TODO works with the return + better node check (no return = ded)

    public SmrtThetaController(Client client) {
        this.client = client;
        this.active = false;

        this.start = new Node(this.client.level, this.client.blockPosition(), new Node(this.client.level, this.client.blockPosition()));
        this.end = new Node(this.client.level, this.client.blockPosition());
        this.open = new HashSet<>(); //new PriorityQueue<>((o1, o2) -> o1.F() == o2.F() ? Double.compare(o1.H(), o2.H()) : Double.compare(o1.F(), o2.F()));
        this.closed = new HashSet<>();

        this.currentTargetPos = new Node(this.client.level, this.client.blockPosition(), Double.MAX_VALUE);
        this.currentTargetPos.setVisible(false);

        this.open.add(this.start);


    }

    public void tick() {
        Entity entity = this.client.nearest(100, 100, 100, entity1 -> entity1 instanceof Player);

        this.releaseAll();

        if (!this.active || entity == null) {
            return;
        }

        this.start.setBlockPos(this.client.blockPosition());
        this.end.setBlockPos(entity.blockPosition());

        if (this.client.blockPosition().equals(this.end.position()) || this.client.eyeBlockPosition().equals(this.end.position())) {
            this.currentTargetPos.setVisible(false);
            return;
        }

        if (this.currentTargetPos.visible()) {
            //this.runForward(true);
            this.client.input.W = true;
            this.client.lookAt(this.currentTargetPos.position());
            Debug.visualizeBlockPosition(this.client.level, this.currentTargetPos.position(), Color.RED, 2.0F);
            if (this.client.blockPosition().equals(this.currentTargetPos.position()) || this.client.eyeBlockPosition().equals(this.currentTargetPos.position())) {
                this.currentTargetPos.setVisible(false);
            }
            return;
            //TODO remove this return so path adjusts while running
        }

        this.calculate();

        for (Node node : this.open) {
            Color color = !node.visible() ? Color.PURPLE : Color.GREEN;
            Debug.visualizeBlockPosition(this.client.level, node.position(), color, 1.0F);
        }
    }

    public void calculate() {
        long stamp1 = System.currentTimeMillis();
        while ((System.currentTimeMillis() - stamp1) <= 30 && !this.open.isEmpty()) {
            Node c = this.low();

            if (!this.lineOfSight(c.level(), c.from().position(), c.position())) {
                c.setFrom(this.bestNearby(c));
            }

            this.open.remove(c);
            this.closed.add(c);
            System.out.println("current F: " + c.F() + " | " + "current open size: " + this.open.size());
            Debug.visualizeBlockPosition(c.level(), c.position(), Color.WHITE, 0.5F);

            //Debug.visualizeBlockPosition(c.level(), this.start.position(), Color.ORANGE, 5.0F);
            //Debug.visualizeBlockPosition(c.level(), this.end.position(), Color.BLUE, 5.0F);

            System.out.println("F: " + c.F() + " | targetF: " + this.currentTargetPos.F());
            if (!c.visible() && this.nextAdjVisible(c) != null && c.F() < this.currentTargetPos.F()) {  //TODO make this check for the better target actually not mess up everything
                System.out.println("target");
                this.currentTargetPos = this.nextAdjVisible(c);
                this.currentTargetPos.setG(c.G());
                this.currentTargetPos.setH(c.H());
                this.open.clear();
                this.open.add(this.start);
                this.closed.clear();
                return;
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
    }

    public Set<Node> successors(Node from) {
        Set<Node> positions = new HashSet<>();
        BlockPos start = new BlockPos(from.position().getX() - 1.0D, from.position().getY() - 1.0D, from.position().getZ() - 1.0D);
        for (int i = start.getX(); i < (start.getX() + 3); i++) {
            for (int j = start.getY(); j < (start.getY() + 3); j++) {
                for (int k = start.getZ(); k < (start.getZ() + 3); k++) {
                    BlockPos position = new BlockPos(i, j, k);
                    Node node = new Node(from.level(), position, from);
                    node.setVisible(MathUtils.distanceSquared(this.client.getEyePosition(), new Vec3(node.position().getX(), node.position().getY(), node.position().getZ()), false, false) < Math.pow(this.radius, 2) && this.client.canSee(node.position()) && !node.position().equals(this.end.position()));
                    if (!position.equals(from.position()) && MathUtils.distanceSquared(this.client.getEyePosition(), new Vec3(node.position().getX(), node.position().getY(), node.position().getZ()), false, false) < Math.pow((this.radius + 1), 2) && this.isPassable(node.position())) {
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
            BlockPos blockPos = node.position().relative(direction);
            if (this.client.canSee(blockPos) && this.isPassable(blockPos)) {
                return new Node(node.level(), blockPos);
            }
        }
        return null;
    }

    public Node nextVisible(Node node) {
        Set<BlockPos> positions = new HashSet<>();
        positions.add(node.position().offset(1, 0, 1));
        positions.add(node.position().offset(1, 0, -1));
        positions.add(node.position().offset(-1, 0, 1));
        positions.add(node.position().offset(-1, 0, -1));
        for (BlockPos blockPos : positions) {
            if (this.client.canSee(blockPos)) {
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
            if (this.client.canSee(from.position().relative(direction)) && MathUtils.distanceSquared(from.position(), this.client.blockPosition(), false, false) < H) {
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
            cost += 10.0D;
        }
        if (this.isPassable(to.position().below())) {
            cost += 5.0D;
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

    private void runForward(boolean space) {
        this.client.input.SPRINT = true;
        this.client.input.W = true;
        if (space) this.client.input.SPACE = true;
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
