package com.swirb.speedrunai.path;

import com.swirb.speedrunai.client.Client;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.NumberConversions;

import java.util.HashSet;
import java.util.Set;

public abstract class Pathing {

    protected final Client client;
    protected final Node start;
    protected final Node end;
    protected final Set<Node> open;
    protected final Set<Node> closed;
    protected final Path path;

    protected double COST_MOVE_ONE = 1.0D;
    protected double COST_MOVE_DIAGONAL = 1.5D;
    protected double COST_MOVE_ELEVATION = 0.5D;
    protected double COST_MOVE_WATER = 0.5D;
    protected double COST_MOVE_LAVA = 8.0D;
    protected double COST_MOVE_AIR = 10.0D;
    protected double COST_MOVE_BLOCKS = 10.0D;

    protected double expandFactor = 1.0D;
    protected Node recreationNode = null;

    public Pathing(Client client, BlockPos start, BlockPos end) {
        this.client = client;
        this.start = new Node(this.client.level, start);
        this.end = new Node(this.client.level, end);
        this.open = new HashSet<>();
        this.closed = new HashSet<>();
        this.path = new Path();
    }

    public Path path() {
        return this.path;
    }

    public void calculate(boolean shortcuts) {
    }

    protected void recreate(boolean shortcuts) {
    }

    protected double estimateCostToEnd(BlockPos start) {
        return this.expandFactor * Math.sqrt(NumberConversions.square(this.end.position().getX() - start.getX()) + NumberConversions.square(this.end.position().getY() - start.getY()) + NumberConversions.square(this.end.position().getZ() - start.getZ()));
    }

    protected double stepCost(Node from, Node to) {
        return 1.0D;
    }

    protected Node getNode(Node node, Set<Node> nodes) {
        for (Node node1 : nodes) {
            if (node1.equals(node)) {
                return node1;
            }
        }
        return null;
    }

    protected Node lowestF(Set<Node> nodes) {
        Node node = null;
        double F = Double.MAX_VALUE;
        for (Node node1 : nodes) {
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

    protected Set<Node> nextTosDiagonal(Node from) {
        return null;
    }

    protected Set<Node> nextTos(Node from) {
        return null;
    }

    protected double distanceSquared(Node from, Node to, boolean horizontal) {
        return horizontal ? NumberConversions.square(to.position().getX() - from.position().getX()) + NumberConversions.square(to.position().getZ() - from.position().getZ()) : NumberConversions.square(to.position().getX() - from.position().getX()) + NumberConversions.square(to.position().getY() - from.position().getY()) + NumberConversions.square(to.position().getZ() - from.position().getZ());
    }

    protected boolean isInLineOfSight(BlockPos from, BlockPos to) {
        if (from == null || to == null) {
            return false;
        }
        Vec3 vec3 = new Vec3(from.getX() + 0.5D, from.getY() + 0.5D, from.getZ() + 0.5D);
        Vec3 vec31 = new Vec3(to.getX() + 0.5D, to.getY() + 0.5D, to.getZ() + 0.5D);
        return this.client.level.clip(new ClipContext(vec3, vec31, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null)).getBlockPos().equals(to);
    }

    protected boolean areAllowedConnect(Node from, Node to) {
        return false;
    }

    protected boolean areDiagonal(Node from, Node to) {
        return NumberConversions.square(to.position().getX() - from.position().getX()) + NumberConversions.square(to.position().getY() - from.position().getY()) + NumberConversions.square(to.position().getZ() - from.position().getZ()) > 1.0D;
    }

    protected boolean areOnTop(BlockPos position0, BlockPos position1) {
        return position0.getX() == position1.getX() && position0.getZ() == position1.getZ();
    }

    protected boolean isPassable(BlockPos position) {
        return this.client.level.getBlockState(position).getCollisionShape(this.client.level, position).isEmpty();
    }
}
