package com.swirb.pvpbots.basicallyGarbage.ai;

public record Node(int id, Type type) {

    public boolean equals(Object o) {
        return o instanceof Node && this.id == ((Node) o).id && this.type == ((Node) o).type;
    }
}
