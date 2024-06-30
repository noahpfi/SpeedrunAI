package com.swirb.pvpbots.basicallyGarbage.ai2;

public class Gene2 {

    private int id;
    private final Node2 from;
    private final Node2 to;
    private double weight;
    private boolean active;

    public Gene2(int id, Node2 from, Node2 to, double weight, boolean enabled) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.active = enabled;
        this.from.setGene(this);
    }

    public int id() {
        return this.id;
    }

    public Node2 from() {
        return this.from;
    }

    public Node2 to() {
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
        return o instanceof Gene2 && this.id == ((Gene2) o).id;
    }

    public Gene2 copy() {
        return new Gene2(this.id, this.from, this.to, this.weight, this.active);
    }

    public String toString() {
        return "gene [id: " + this.id + ", from: {id: " + this.from.id() + ", type: " + this.from.type().toString() + "}, to: {id: " + this.to.id() + ", type: " + this.to.type().toString() + "}, weight: " + this.weight + ", enabled: " + this.active + "]";
    }
}
