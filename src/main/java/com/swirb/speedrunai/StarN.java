package com.swirb.speedrunai;

import net.minecraft.core.BlockPos;

public class StarN {

    private final BlockPos blockPos;
    private StarN pointer;
    private double G;
    private double H;
    private double F;

    public StarN(BlockPos blockPos) {
        this.blockPos = blockPos;
        this.pointer = new StarN(blockPos, this);
        this.G = 0.0D;
        this.H = 0.0D;
        this.sync();
    }

    public StarN(BlockPos blockPos, StarN pointer) {
        this.blockPos = blockPos;
        this.pointer = pointer;
        this.G = 0.0D;
        this.H = 0.0D;
        this.sync();
    }

    public StarN(BlockPos blockPos, double G, double H) {
        this.blockPos = blockPos;
        this.pointer = new StarN(blockPos, this);
        this.G = G;
        this.H = H;
        this.sync();
    }

    public StarN(BlockPos blockPos, StarN pointer, double G, double H) {
        this.blockPos = blockPos;
        this.pointer = pointer;
        this.G = G;
        this.H = H;
        this.sync();
    }

    private void sync() {
        this.F = this.G + this.H;
    }

    public void setPointer(StarN pointer) {
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

    public StarN pointer() {
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
        return o instanceof StarN && ((StarN) o).blockPos.equals(this.blockPos);
    }
}
