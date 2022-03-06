package com.swirb.speedrunai.path;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.client.Client;
import com.swirb.speedrunai.main.SpeedrunAI;
import com.swirb.speedrunai.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class ThetaStar extends Pathing {

    public ThetaStar(Client client, BlockPos start, BlockPos end) {
        super(client, start, end);
        this.open.add(this.start);
    }

    public void calculate(boolean shortcuts) {
        long stamp = System.currentTimeMillis();
        new BukkitRunnable() {
            public void run() {
                long stamp1 = System.currentTimeMillis();
                while ((System.currentTimeMillis() - stamp1) <= 30) {
                    SpeedrunAI.getInstance().getLogger().info(client.getName()
                            + " is processing a path of length " + open.size());
                    if (ThetaStar.this.open.isEmpty()) {
                        SpeedrunAI.getInstance().getLogger().info(client.getName()
                                + " ran out of nodes in " + ((System.currentTimeMillis() - stamp) / 1000.0D) + "s !");
                        this.cancel();
                        break;
                    }
                    Node current = ThetaStar.this.lowestF(ThetaStar.this.open);
                    Debug.visualizeBlockPosition(ThetaStar.this.client.level, current.position(), Color.TEAL, 2.0F);
                    if (current.equals(ThetaStar.this.end)) {
                        SpeedrunAI.getInstance().getLogger().info(client.getName()
                                + " found a path in " + ((System.currentTimeMillis() - stamp) / 1000.0D) + "s !");
                        ThetaStar.this.end.setFrom(current.from());
                        ThetaStar.this.recreate(shortcuts);
                        this.cancel();
                        break;
                    }
                    ThetaStar.this.open.remove(current);
                    ThetaStar.this.closed.add(current);
                    for (Node adjacent : ThetaStar.this.nextTos(current)) {
                        if (adjacent.equals(ThetaStar.this.end)) {
                            SpeedrunAI.getInstance().getLogger().info(client.getName()
                                    + " found a path in " + ((System.currentTimeMillis() - stamp) / 1000.0D) + "s !");
                            ThetaStar.this.end.setFrom(current.from());
                            ThetaStar.this.recreate(shortcuts);
                            this.cancel();
                            break;
                        }
                        Node existingOpen = ThetaStar.this.getNode(adjacent, ThetaStar.this.open);
                        Node existingClosed = ThetaStar.this.getNode(adjacent, ThetaStar.this.closed);
                        if (current.from() != null && ThetaStar.this.isInLineOfSight(current.from().position(), adjacent.position())/* && !ThetaStar.this.client.level.getBlockState(adjacent.position().below()).isAir()*/) {  //TODO that's what I want, BUT IS FUKIN SLOW
                            if (existingOpen != null && current.from().G() + ThetaStar.this.stepCost(current.from(), existingOpen) < existingOpen.G()) {
                                existingOpen.setG(current.from().G() + ThetaStar.this.stepCost(current.from(), adjacent));
                                existingOpen.setFrom(current.from());
                            }
                            if (existingClosed != null && current.from().G() + ThetaStar.this.stepCost(current.from(), existingClosed) < existingClosed.G()) {
                                ThetaStar.this.closed.remove(existingClosed);
                            }
                            if (existingOpen == null && existingClosed == null) {
                                adjacent.setG(current.from().G() + ThetaStar.this.stepCost(current.from(), adjacent));
                                adjacent.setFrom(current.from());
                                ThetaStar.this.open.add(adjacent);
                            }
                        }
                        else {
                            if (existingOpen != null && adjacent.G() < existingOpen.G()) {
                                existingOpen.setG(adjacent.G());
                                existingOpen.setFrom(current);
                            }
                            if (existingClosed != null && adjacent.G() < existingClosed.G()) {
                                ThetaStar.this.closed.remove(existingClosed);
                            }
                        }
                        if (existingOpen == null && existingClosed == null) {
                            ThetaStar.this.open.add(adjacent);
                        }
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
    }

    public void recreate(boolean shortcuts) {
        long stamp = System.currentTimeMillis();
        this.recreationNode = this.end;
        new BukkitRunnable() {
            public void run() {
                long stamp1 = System.currentTimeMillis();
                while ((System.currentTimeMillis() - stamp1) <= 30) {
                    SpeedrunAI.getInstance().getLogger().info(client.getName()
                            + " is recreating a path of length " + path.length());
                    if (ThetaStar.this.recreationNode.from() != null) {
                        ThetaStar.this.recreationNode = ThetaStar.this.recreationNode.from();
                        path.add(0, ThetaStar.this.recreationNode);
                    }
                    else {
                        path.add(ThetaStar.this.end);
                        path.setFound(true);
                        SpeedrunAI.getInstance().getLogger().info(client.getName()
                                + " recreated a path in " + ((System.currentTimeMillis() - stamp) / 1000.0D) + "s !");
                        System.out.println(path.nodes().size());
                        final int[] i = {0};
                        new BukkitRunnable() {
                            public void run() {
                                i[0]++;
                                for (Node node : path.nodes()) {
                                    Debug.visualizeBlockPosition(node.level(), node.position(), Color.RED, 2.0F);
                                }
                                if (i[0] == 200) this.cancel();
                            }
                        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 1);
                        this.cancel();
                        break;
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
    }

    //TODO refine cost
    protected double stepCost(Node from, Node to) {
        double speed = this.client.level.getBlockState(to.position()).getDestroySpeed(this.client.level, to.position());
        if (speed == -1) speed = Double.MAX_VALUE;
        double cost = MathUtils.distanceSquared(from.position(), to.position(), true, false) + (to.walkable() ? (this.areDiagonal(from, to) ? this.COST_MOVE_DIAGONAL : this.COST_MOVE_ONE) : this.COST_MOVE_BLOCKS * speed);
        return cost;
    }

    protected Set<Node> nextTosDiagonal(Node from) {
        Set<Node> positions = new HashSet<>();
        BlockPos start = new BlockPos(from.position().getX() - 1.0D, from.position().getY() - 1.0D, from.position().getZ() - 1.0D);
        for (int i = start.getX(); i < (start.getX() + 3); i++) {
            for (int j = start.getY(); j < (start.getY() + 3); j++) {
                for (int k = start.getZ(); k < (start.getZ() + 3); k++) {
                    BlockPos position = new BlockPos(i, j, k);
                    Node node = new Node(from.level(), position, from, 0.0D, this.estimateCostToEnd(position));
                    node.setG(from.G() + this.stepCost(from, node));
                    if (!node.equals(from)) {
                        positions.add(node);
                    }
                }
            }
        }
        return positions;
    }

    protected Set<Node> nextTos(Node from) {
        Set<Node> positions = new HashSet<>();
        for (Direction direction : Direction.values()) {
            Node node = new Node(from.level(), from.position().relative(direction), from, 0.0D, this.estimateCostToEnd(from.position().relative(direction)));
            node.setG(from.G() + this.stepCost(from, node));
            positions.add(node);
        }
        return positions;
    }

    /*
    private boolean isWaterAtBottom(BlockPos position) {
        for (int i = position.getY(); i >= 3; i--) {
            BlockPos position1 = new BlockPos(position.getX(), i, position.getZ());
            if (this.client.world.getType(position1).getMaterial() == Material.WATER && this.isInLineOfSight(position, position1)) {
                return true;
            }
        }
        return false;
    }
     */
}
