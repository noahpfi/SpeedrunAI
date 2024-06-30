package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.swirb.pvpbots.basicallyGarbage.ai.Type;

public class Connection3 {

    private final int id;
    private final Node3 from;
    private final Node3 to;
    private double weight;
    private boolean active;

    public Connection3(int id, Node3 from, Node3 to, double weight, boolean active) {
        if (from.type() == Type.OUTPUT || to.type() == Type.INPUT) {
            throw new AssertionError("[AI] -> getConnection: nodes don't meet getConnection criteria");
        }
        this.id = id;
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.active = active;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int id() {
        return this.id;
    }

    public Node3 from() {
        return this.from;
    }

    public Node3 to() {
        return this.to;
    }

    public double weight() {
        return this.weight;
    }

    public boolean active() {
        return this.active;
    }

    public Connection3 copy() {
        return new Connection3(this.id, this.from.copy(), this.to.copy(), this.weight, this.active);
    }

    public boolean equals(Object o) {
        return o instanceof Connection3 && ((Connection3) o).id == this.id;
    }
}
