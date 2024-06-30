package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.swirb.pvpbots.basicallyGarbage.ai.Activation;
import com.swirb.pvpbots.basicallyGarbage.ai.Type;

public class Node2 {

    private final int id;
    private final Type type;
    private double value;
    private Gene2 gene;

    public Node2(int id, Type type) {
        this.id = id;
        this.type = type;
    }

    public void calculateNext(Activation activation) {
        if (this.type == Type.OUTPUT) {
            return;
        }
        if (this.gene == null) {
            throw new AssertionError("[AI] Node: gene is null");
        }
        if (this.gene.to() == null) {
            throw new AssertionError("[AI] Node: next node is null");
        }
        this.gene.to().value = activation.activate(this.value * this.gene.weight());
        this.gene.to().calculateNext(activation);
    }

    public boolean willCauseRecursion(Node2 node) {
        if (this.type == Type.OUTPUT) {
            return false;
        }
        if (this.gene == null) {
            throw new AssertionError("[AI] Node: gene is null");
        }
        if (this.gene.to().equals(node)) {
            return true;
        }
        if (this.gene.to() == null) {
            return false;
        }
        return this.gene.to().willCauseRecursion(node);
    }

    public void setGene(Gene2 gene) {
        this.gene = gene;
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

    public Gene2 gene() {
        return this.gene;
    }

    public boolean equals(Object o) {
        return o instanceof Node2 && this.id == ((Node2) o).id && this.type == ((Node2) o).type;
    }
}
