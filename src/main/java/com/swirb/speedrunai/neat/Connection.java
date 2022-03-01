package com.swirb.speedrunai.neat;

public class Connection {

    private Node inNode;
    private Node outNode;
    private float weight;
    private boolean expressed;
    private int innovation;

    public Connection(Node inNode, Node outNode, float weight, boolean expressed, int innovation) {
        this.inNode = inNode;
        this.outNode = outNode;
        this.weight = weight;
        this.expressed = expressed;
        this.innovation = innovation;
    }

    public Connection copy() {
        return new Connection(this.inNode, this.outNode, this.weight, this.expressed, this.innovation);
    }

    public Node inNode() {
        return this.inNode;
    }

    public Node outNode() {
        return this.outNode;
    }

    public float weight() {
        return this.weight;
    }

    public boolean expressed() {
        return this.expressed;
    }

    public int innovation() {
        return this.innovation;
    }

    public void disable() {
        this.expressed = false;
    }
}
