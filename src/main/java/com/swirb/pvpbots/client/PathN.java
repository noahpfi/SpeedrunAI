package com.swirb.pvpbots.client;

import net.minecraft.core.BlockPos;

public class PathN {

    private final BlockPos blockPos;
    private PathN pointer;
    private double G;
    private double H;
    private double F;

    public PathN(BlockPos blockPos) {
        this.blockPos = blockPos;
        this.pointer = new PathN(blockPos, null);
        this.G = 0.0D;
        this.H = 0.0D;
        this.sync();
    }

    public PathN(BlockPos blockPos, PathN pointer) {
        this.blockPos = blockPos;
        this.pointer = pointer;
        this.G = 0.0D;
        this.H = 0.0D;
        this.sync();
    }

    public PathN(BlockPos blockPos, double G, double H) {
        this.blockPos = blockPos;
        this.pointer = new PathN(blockPos, null);
        this.G = G;
        this.H = H;
        this.sync();
    }

    public PathN(BlockPos blockPos, PathN pointer, double G, double H) {
        this.blockPos = blockPos;
        this.pointer = pointer;
        this.G = G;
        this.H = H;
        this.sync();
    }

    private void sync() {
        this.F = this.G + this.H;
    }

    public void setPointer(PathN pointer) {
        this.pointer = pointer;
    }

    public void setG(double G) {
        this.G = G;
        this.sync();
    }

    public void setH(double H) {
        this.H = H;
        this.sync();
    }

    public BlockPos blockPos() {
        return this.blockPos;
    }

    public PathN pointer() {
        return this.pointer;
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

    public boolean equals(Object o) {
        return o instanceof PathN && ((PathN) o).blockPos.equals(this.blockPos);
    }
}
