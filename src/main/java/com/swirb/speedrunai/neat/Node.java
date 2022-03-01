package com.swirb.speedrunai.neat;

public class Node {

    private TYPE type;
    private int innovation;

    public Node(TYPE type, int innovation) {
        this.type = type;
        this.innovation = innovation;
    }

    public Node copy() {
        return new Node(this.type, this.innovation);
    }

    public TYPE type() {
        return this.type;
    }

    public int innovation() {
        return this.innovation;
    }

    public enum TYPE {
        INPUT,
        OUTPUT,
        HIDDEN
    }
}
