package com.swirb.speedrunai.path;

import com.swirb.speedrunai.Debug;
import com.swirb.speedrunai.client.Client;
import com.swirb.speedrunai.main.SpeedrunAI;
import net.minecraft.core.BlockPos;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class LazyAStar extends Pathing {

    private final Set<BlockPos> range;

    public LazyAStar(Client client, BlockPos start, BlockPos end, Set<BlockPos> range) {
        super(client, start, end);
        this.open.add(this.start);
        this.range = range;
    }

    public void calculate(boolean shortcuts) {
        long stamp = System.currentTimeMillis();
        new BukkitRunnable() {
            public void run() {
                long stamp1 = System.currentTimeMillis();
                while ((System.currentTimeMillis() - stamp1) <= 30) {
                    SpeedrunAI.LOGGER.info("[{}] processing path... ({})", LazyAStar.this.client.getName(), LazyAStar.this.open.size());
                    if (LazyAStar.this.open.isEmpty()) {
                        SpeedrunAI.LOGGER.info("[{}] ran out of nodes ({}s)", LazyAStar.this.client.getName(), ((System.currentTimeMillis() - stamp) / 1000.0D));
                        this.cancel();
                        break;
                    }
                    Node current = LazyAStar.this.lowestF(LazyAStar.this.open);
                    Debug.visualizeBlockPosition(LazyAStar.this.client.level, current.position(), Color.GRAY, 2.0F);
                    if (current.equals(LazyAStar.this.end)) {
                        SpeedrunAI.LOGGER.info("[{}] path found! ({}s)", LazyAStar.this.client.getName(), ((System.currentTimeMillis() - stamp) / 1000.0D));
                        LazyAStar.this.end.setFrom(current.from());
                        LazyAStar.this.recreate(shortcuts);
                        this.cancel();
                        break;
                    }
                    LazyAStar.this.open.remove(current);
                    LazyAStar.this.closed.add(current);
                    for (Node adjacent : LazyAStar.this.nextTosDiagonal(current)) {
                        Node existingOpen = LazyAStar.this.getNode(adjacent, LazyAStar.this.open);
                        Node existingClosed = LazyAStar.this.getNode(adjacent, LazyAStar.this.closed);
                        if (existingOpen != null && adjacent.G() < existingOpen.G()) {
                            existingOpen.setG(adjacent.G());
                            existingOpen.setFrom(current);
                        }
                        if (existingClosed != null && adjacent.G() < existingClosed.G()) {
                            LazyAStar.this.closed.remove(existingClosed);
                        }
                        if (existingOpen == null && existingClosed == null) {
                            LazyAStar.this.open.add(adjacent);
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
                    SpeedrunAI.LOGGER.info("[{}] recreating path... ({})", LazyAStar.this.client.getName(), path.length());
                    if (LazyAStar.this.recreationNode.from() != null) {
                        LazyAStar.this.recreationNode = LazyAStar.this.recreationNode.from();
                        path.add(0, LazyAStar.this.recreationNode);
                    }
                    else {
                        path.add(LazyAStar.this.end);
                        if (shortcuts) {
                            Set<Node> cut = new HashSet<>();
                            Node referenceNode = null;
                            for (Node node : path.nodes()) {
                                Node after = path.nodeAfter(node);
                                if (LazyAStar.this.areAllowedConnect(referenceNode, after)) {
                                    cut.add(after);
                                    continue;
                                }
                                referenceNode = node;
                            }
                            int length = path.length();
                            path.cutOutNodes(cut);
                            SpeedrunAI.LOGGER.info("[{}] applied shortcuts ({}|{})", LazyAStar.this.client.getName(), length, path.length());
                        }
                        path.setFound(true);
                        SpeedrunAI.LOGGER.info("[{}] path recreated! ({}s)", LazyAStar.this.client.getName(), ((System.currentTimeMillis() - stamp) / 1000D));
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

    protected double stepCost(Node from, Node to) {
        return (to.walkable() ? (this.areDiagonal(from, to) ? this.COST_MOVE_DIAGONAL : this.COST_MOVE_ONE) : this.COST_MOVE_BLOCKS) + (Math.abs(from.position().getY() - to.position().getY()) > 1.0D ? (this.isPassable(to.position().below()) && !this.areOnTop(from.position(), to.position()) ? this.COST_MOVE_AIR + this.COST_MOVE_ELEVATION : this.COST_MOVE_ELEVATION) : (this.isPassable(to.position().below()) ? this.COST_MOVE_AIR : 0.0D));
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
                    if (!node.equals(from) && this.range.contains(node.position())) {
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

    public Set<BlockPos> range() {
        return this.range;
    }
}
