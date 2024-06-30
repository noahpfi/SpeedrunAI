package com.swirb.pvpbots.basicallyGarbage.ai3;

import com.swirb.pvpbots.basicallyGarbage.ai.Type;

public class Node3 {

    private final int id;
    private final Type type;
    private double value;

    public Node3(int id, Type type, double value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int id() {
        return this.id;
    }

    public Type type() {
        return this.type;
    }

    public double value() {
        return this.value;
    }

    public Node3 copy() {
        return new Node3(this.id, this.type, this.value);
    }

    public boolean equals(Object o) {
        return o instanceof Node3 && ((Node3) o).id == this.id;
    }
}
