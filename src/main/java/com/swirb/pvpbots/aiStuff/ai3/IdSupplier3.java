package com.swirb.pvpbots.basicallyGarbage.ai3;

public class IdSupplier3 {

    private int nodeId;
    private int connectionId;
    private int genomeId;
    private int speciesId;
    private int generationId;

    public IdSupplier3() {
        this.nodeId = 0;
        this.connectionId = 0;
        this.genomeId = 0;
        this.speciesId = 0;
        this.generationId = 0;
    }

    public int nextNodeId() {
        return this.nodeId++;
    }

    public int nextConnectionId() {
        return this.connectionId++;
    }

    public int nextGenomeId() {
        return this.genomeId++;
    }

    public int nextSpeciesId() {
        return this.speciesId++;
    }

    public int nextGenerationId() {
        return this.generationId++;
    }
}
