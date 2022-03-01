package com.swirb.speedrunai.path;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.client.Client;
import com.swirb.speedrunai.main.SpeedrunAI;
import net.minecraft.core.BlockPos;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class AStar extends Pathing {

    public AStar(Client client, BlockPos start, BlockPos end) {
        super(client, start, end);
        this.open.add(this.start);
        this.expandFactor = 3.01D;
    }

    public void calculate(boolean shortcuts) {
        long stamp = System.currentTimeMillis();
        new BukkitRunnable() {
            public void run() {
                long stamp1 = System.currentTimeMillis();
                while ((System.currentTimeMillis() - stamp1) <= 30) {
                    SpeedrunAI.LOGGER.info("[{}] processing path... ({})", AStar.this.client.getName().getString(), AStar.this.open.size());
                    if (AStar.this.open.isEmpty()) {
                        SpeedrunAI.LOGGER.info("[{}] ran out of nodes ({}s)", AStar.this.client.getName().getString(), ((System.currentTimeMillis() - stamp) / 1000.0D));
                        this.cancel();
                        break;
                    }
                    Node current = AStar.this.lowestF(AStar.this.open);
                    Debug.visualizeBlockPosition(AStar.this.client.level, current.position(), Color.GRAY, 2.0F);
                    if (current.equals(AStar.this.end)) {
                        SpeedrunAI.LOGGER.info("[{}] path found! ({}s)", AStar.this.client.getName().getString(), ((System.currentTimeMillis() - stamp) / 1000.0D));
                        AStar.this.end.setFrom(current.from());
                        AStar.this.recreate(shortcuts);
                        this.cancel();
                        break;
                    }
                    AStar.this.open.remove(current);
                    AStar.this.closed.add(current);
                    for (Node adjacent : AStar.this.nextTosDiagonal(current)) {
                        Node existingOpen = AStar.this.getNode(adjacent, AStar.this.open);
                        Node existingClosed = AStar.this.getNode(adjacent, AStar.this.closed);
                        if (existingOpen != null && adjacent.G() < existingOpen.G()) {
                            existingOpen.setG(adjacent.G());
                            existingOpen.setFrom(current);
                        }
                        if (existingClosed != null && adjacent.G() < existingClosed.G()) {
                            AStar.this.closed.remove(existingClosed);
                        }
                        if (existingOpen == null && existingClosed == null) {
                            AStar.this.open.add(adjacent);
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
                    SpeedrunAI.LOGGER.info("[{}] recreating path... ({})", AStar.this.client.getName().getString(), path.length());
                    if (AStar.this.recreationNode.from() != null) {
                        AStar.this.recreationNode = AStar.this.recreationNode.from();
                        path.add(0, AStar.this.recreationNode);
                    }
                    else {
                        path.add(AStar.this.end);
                        if (shortcuts) {
                            Set<Node> cut = new HashSet<>();
                            Node referenceNode = null;
                            for (Node node : path.nodes()) {
                                Node after = path.nodeAfter(node);
                                if (AStar.this.areAllowedConnect(referenceNode, after)) {
                                    cut.add(after);
                                    continue;
                                }
                                referenceNode = node;
                            }
                            int length = path.length();
                            path.cutOutNodes(cut);
                            SpeedrunAI.LOGGER.info("[{}] applied shortcuts ({}|{})", AStar.this.client.getName().getString(), length, path.length());
                        }
                        path.setFound(true);
                        SpeedrunAI.LOGGER.info("[{}] path recreated! ({}s)", AStar.this.client.getName().getString(), ((System.currentTimeMillis() - stamp) / 1000D));
                        System.out.println(path.nodes().size());
                        for (Node node : path.nodes()) {
                            Debug.visualizeBlockPosition(node.level(), node.position(), Color.RED, 5.0F);
                        }
                        this.cancel();
                        break;
                    }
                }
            }
        }.runTaskTimer(SpeedrunAI.getInstance(), 0, 0);
    }

    //TODO refine cost
    protected double stepCost(Node from, Node to) {
        double cost = this.areDiagonal(from, to) ? this.COST_MOVE_DIAGONAL : this.COST_MOVE_ONE;
        /*
        // if not walkable (blocks in the way)
        //TODO don't go through diagonal corners
        if (!to.walkable()) {
            /
            if (this.client.inventoryUtils.hasToolFor(from.world().getType(from.position())) && this.client.inventoryUtils.hasToolFor(from.world().getType(from.position().up()))) {
                cost += this.COST_MOVE_BLOCKS * 0.5D;
            }
            else cost += this.COST_MOVE_BLOCKS;
             /
        }
        // if liquid (lava, water)
        if (to.world().getType(to.position()).getMaterial().isLiquid() || to.world().getType(to.position().down()).getMaterial().isLiquid()) {
            if (to.world().getType(to.position()).getMaterial() == Material.LAVA || to.world().getType(to.position().down()).getMaterial() == Material.LAVA) {
                cost += this.COST_MOVE_LAVA;
            }
            else cost += this.COST_MOVE_WATER;
        }
        // if no block below to (in air)
        if (to.world().getType(to.position().down()).getCollisionShape(this.client.world, to.position().down()).isEmpty() && !(to.world().getType(to.position().down()).getMaterial() == Material.WATER) && !(to.world().getType(to.position().down()).getMaterial() == Material.LAVA)) {
            // if from and to are the same height
            if (from.position().getY() == to.position().getY()) {
                cost += this.COST_MOVE_AIR;
            }
            // if path goes down
            else if ((from.position().getY() - to.position().getY()) >= 1) {
                // if to is directly below from
                if (from.position().down().equals(to.position())) {
                    // if client has mlg or water all the way down
                    if (this.client.inventoryUtils.hasMLG() || this.isWaterAtBottom(to.position())) {
                        cost += 0.0D;
                    }
                    // doesn't have mlg
                    else {
                        cost += this.COST_MOVE_AIR * 0.05D;
                    }
                }
                // not directly below (diagonal) [when falling down the client can't move much in a direction, so make it dislike going diagonal downwards in air]
                else {
                    cost += this.COST_MOVE_AIR;
                }
            }
            // if path goes up
            else if ((to.position().getY() - from.position().getY()) >= 1) {
                // if to is directly above from
                if (from.position().up().equals(to.position())) {
                    // if to is next to a wall
                    if (!this.isPassable(to.position().north()) || !this.isPassable(to.position().south()) || !this.isPassable(to.position().west()) || !this.isPassable(to.position().east())) {
                        cost += this.COST_MOVE_ELEVATION;
                    }
                    // not next to wall
                    else {
                        cost += this.COST_MOVE_ELEVATION * 1.25D;
                    }
                }
                // not directly above (diagonal)
                else {
                    cost += this.COST_MOVE_AIR + this.COST_MOVE_ELEVATION;
                }
            }
        }
        else {
            // if path goes up
            if ((to.position().getY() - from.position().getY()) >= 1) {
                cost += this.COST_MOVE_ELEVATION;
            }
        }
         */
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

    protected boolean areAllowedConnect(Node from, Node to) {
        return from != null && to != null && from.level() == to.level() && ((!to.equals(this.end) && Math.abs(to.position().getY() - from.position().getY()) < 1 && this.isInLineOfSight(from.position(), to.position()) && this.distanceSquared(from, to, true) <= (3 * 3)) || (this.areOnTop(from.position(), to.position()) && (from.position().getY() - to.position().getY()) >= 1));
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
