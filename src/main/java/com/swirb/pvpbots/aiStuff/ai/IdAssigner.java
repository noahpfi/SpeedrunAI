package com.swirb.pvpbots.basicallyGarbage.ai;

public class IdAssigner {

    private int id;

    public IdAssigner() {
        this.id = 0;
    }

    public int nextId() {
        int c = id;
        id++;
        return c;
    }
}
