package com.swirb.pvpbots.basicallyGarbage.ai;

import java.util.*;

public class Species {

    private final AI ai;
    private final int id;
    private Genome genome;
    private final Set<Genome> genomes;
    private double maxFitness;
    private int generationsFailed;

    public Species(AI ai, Genome genome) {
        this.ai = ai;
        this.id = this.ai.idAssigner().nextId();
        this.genome = genome;
        this.genomes = new HashSet<>();
        this.genomes.add(genome);
        this.updateFitness();
        this.maxFitness = 0.0D;
        this.generationsFailed = 0;
    }

    public void updateFitness() {
        double fitness = 0.0D;
        for (Genome genome : this.genomes) {
            if (genome.fitness() > fitness) {
                fitness = genome.fitness();
            }
        }
        if (fitness > this.maxFitness) {
            this.generationsFailed = 0;
        }
        this.maxFitness = fitness;
    }

    public double avgFitness() {
        double fitness = 0.0D;
        for (Genome genome : this.genomes) {
            fitness += genome.fitness();
        }
        return fitness / this.genomes.size();
    }

    public List<Genome> best() {
        List<Genome> best = new ArrayList<>(this.genomes);
        best.sort(Comparator.comparing(Genome::fitness));
        return best;
    }

    public void update() {
        this.genome = Rng.random(this.genomes);
    }

    public void addGenome(Genome genome) {
        this.genomes.add(genome);
    }

    public void removeGenome(Genome genome) {
        this.genomes.remove(genome);
    }

    public void clearGenomes() {
        this.genomes.clear();
    }

    public boolean compatible(Genome genome) {
        return this.genome.compatibilityDistance(this.genome, genome) <= this.ai.setting().COMP_DISTANCE_SPECIES_MAX;
    }

    public void failed() {
        this.generationsFailed++;
    }

    public int id() {
        return this.id;
    }

    public Genome genome() {
        return this.genome;
    }

    public Set<Genome> genomes() {
        return this.genomes;
    }

    public double maxFitness() {
        return this.maxFitness;
    }

    public int generationsFailed() {
        return this.generationsFailed;
    }
}
