package com.swirb.pvpbots.basicallyGarbage.ai;

public record Connection(Node from, Node to) {

    public boolean equals(Object o) {
        return o instanceof Connection && this.from.equals(((Connection) o).from) && this.to.equals(((Connection) o).to);
    }
}
