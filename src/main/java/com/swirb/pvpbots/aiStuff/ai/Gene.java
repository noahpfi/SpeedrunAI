package com.swirb.pvpbots.basicallyGarbage.ai;

public class Gene implements Cloneable {

    private int id;
    private final Node from;
    private final Node to;
    private double weight;
    private boolean active;

    public Gene(int id, Node from, Node to, double weight, boolean enabled) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.active = enabled;
    }

    public int id() {
        return this.id;
    }

    public Node from() {
        return this.from;
    }

    public Node to() {
        return this.to;
    }

    public double weight() {
        return this.weight;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean equals(Object o) {
        return o instanceof Gene && this.id == ((Gene) o).id;
    }

    public Gene clone() {
        return new Gene(this.id, this.from, this.to, this.weight, this.active);
    }

    public String toString() {
        return "gene [id: " + this.id + ", from: {id: " + this.from.id() + ", type: " + this.from.type().toString() + "}, to: {id: " + this.to.id() + ", type: " + this.to.type().toString() + "}, weight: " + this.weight + ", enabled: " + this.active + "]";
    }
}
