package com.swirb.pvpbots.basicallyGarbage.ai2;

import com.swirb.pvpbots.basicallyGarbage.ai.Rng;

import java.util.*;

public class Species2 {

    private final AI2 ai;
    private final int id;
    private final Set<Genome2> genomes;
    private Genome2 selected;
    private double maxFitness;
    private int generationsFailed;

    public Species2(AI2 ai, Genome2 genome) {
        this.ai = ai;
        this.id = this.ai.idAssigner().nextId();
        this.genomes = new HashSet<>();
        this.genomes.add(genome);
        this.selected = genome;
        this.maxFitness = 0.0D;
        this.generationsFailed = 0;
    }

    public void addGenome(Genome2 genome) {
        this.genomes.add(genome);
    }

    public void removeGenome(Genome2 genome) {
        this.genomes.remove(genome);
    }

    public void clearGenomes() {
        this.genomes.clear();
    }

    public void calculateEntirely() {
        for (Genome2 genome : this.genomes) {
            genome.calculateEntirely();
        }
    }

    public void applyOutputs() {
        for (Genome2 genome : this.genomes) {
            genome.applyOutputs();
        }
    }

    public void updateFitness() {
        double fitness = 0.0D;
        for (Genome2 genome : this.genomes) {
            if (genome.fitness() > fitness) {
                fitness = genome.fitness();
            }
        }
        if (fitness > this.maxFitness) {
            this.generationsFailed = 0;
        }
        this.maxFitness = fitness;
    }

    public List<Genome2> bestGenomes() {
        if (this.genomes.isEmpty()) {
            throw new AssertionError("[AI] bestGenomes: species has 0 genomes");
        }
        List<Genome2> best = new ArrayList<>(this.genomes);
        best.sort(Comparator.comparing(Genome2::fitness));
        return best;
    }

    public void update() {
        this.selected = Rng.random(this.genomes);
    }

    public void upFailed() {
        this.generationsFailed++;
    }

    public boolean isCompatible(Genome2 genome) {
        return this.selected.compatibilityDistance(genome) <= this.ai.setting().COMP_DISTANCE_SPECIES_MAX;
    }

    public double avgFitness() {
        double fitness = 0.0D;
        for (Genome2 genome : this.genomes) {
            fitness += genome.fitness();
        }
        return fitness / this.genomes.size();
    }

    public int id() {
        return this.id;
    }

    public Set<Genome2> genomes() {
        return this.genomes;
    }

    public double maxFitness() {
        return this.maxFitness;
    }

    public int generationsFailed() {
        return this.generationsFailed;
    }

    public boolean equals(Object o) {
        return o instanceof Species2 && ((Species2) o).id == this.id;
    }
}
