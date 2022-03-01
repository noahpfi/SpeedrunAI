package com.swirb.speedrunai.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class Node {

    private BlockPos blockPos;
    private final Level level;
    private Node from;
    private double G;
    private double H;
    private double F;
    private boolean visible;
    private final boolean walkable;

    public Node(Level level, BlockPos blockPosition, Node from, double G, double H) {
        this.blockPos = blockPosition;
        this.level = level;
        this.from = from;
        this.G = G;
        this.H = H;
        this.recalculate();
        this.visible = true;
        this.walkable = this.setWalkable();
    }

    public Node(Level level, BlockPos blockPosition, Node from) {
        this.blockPos = blockPosition;
        this.level = level;
        this.from = from;
        this.G = 0.0D;
        this.H = 0.0D;
        this.recalculate();
        this.visible = true;
        this.walkable = this.setWalkable();
    }

    public Node(Level level, BlockPos blockPosition, boolean visible) {
        this.blockPos = blockPosition;
        this.level = level;
        this.G = 0.0D;
        this.H = 0.0D;
        this.recalculate();
        this.visible = visible;
        this.walkable = this.setWalkable();
    }

    public Node(Level level, BlockPos blockPosition, double G, double H) {
        this.blockPos = blockPosition;
        this.level = level;
        this.G = G;
        this.H = H;
        this.recalculate();
        this.visible = true;
        this.walkable = this.setWalkable();
    }

    public Node(Level level, BlockPos blockPosition, double H) {
        this.blockPos = blockPosition;
        this.level = level;
        this.G = 0.0D;
        this.H = H;
        this.recalculate();
        this.visible = true;
        this.walkable = this.setWalkable();
    }

    public Node(Level level, BlockPos blockPosition) {
        this.blockPos = blockPosition;
        this.level = level;
        this.G = 0.0D;
        this.H = 0.0D;
        this.recalculate();
        this.visible = true;
        this.walkable = this.setWalkable();
    }

    private boolean setWalkable() {
        return this.level.getBlockState(this.blockPos).getCollisionShape(this.level, this.blockPos).isEmpty() && this.level.getBlockState(this.blockPos.above()).getCollisionShape(this.level, this.blockPos.above()).isEmpty();
    }

    private void recalculate() {
        this.F = this.G + this.H;
    }

    public BlockPos position() {
        return this.blockPos;
    }

    public Level level() {
        return this.level;
    }

    public Node from() {
        return this.from;
    }

    public double G() {
        return this.G;
    }

    public double H() {
        return this.H;
    }

    public double F() {
        return this.F;
    }

    public boolean visible() {
        return this.visible;
    }

    public boolean walkable() {
        return this.walkable;
    }

    public void setFrom(Node from) {
        this.from = from;
    }

    public void setG(double g) {
        this.G = g;
        this.recalculate();
    }

    public void setH(double h) {
        this.H = h;
        this.recalculate();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public boolean equals(Object o) {
        return o instanceof Node && (this.blockPos.equals(((Node) o).position()) && this.level.equals(((Node) o).level()));
    }
}
